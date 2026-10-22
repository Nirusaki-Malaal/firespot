package com.fireemu.ui.screens.browser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.fireemu.FireEmuApp
import com.fireemu.data.repository.CartridgeImporter
import kotlinx.coroutines.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Website browsing is separate from the signed native catalog and is destroyed on exit. */
class GameBrowserActivity : ComponentActivity() {
    companion object { const val HOME="https://www.romsgames.net/roms/" }
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private var transfer: Job?=null
    private var connection: HttpURLConnection?=null
    private var pending: File?=null
    private var destroyed=false
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(12,12,12));setPadding(24,12,24,12)}
        val toolbar=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL}
        fun button(label:String, action:()->Unit) {toolbar.addView(Button(this).apply {text=label;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(38,38,38));setOnClickListener {action()}},LinearLayout.LayoutParams(0,48.dp(),1f).apply {setMargins(0,0,8,0)})}
        button("Back") {goBack()}
        button("Consoles") {web.loadUrl(HOME)}
        button("Reload") {web.reload()}
        button("Cancel download") {transfer?.cancel();connection?.disconnect()}
        button("My library") {finish()}
        root.addView(toolbar)
        status=TextView(this).apply {setTextColor(Color.LTGRAY);textSize=13f;setPadding(8,12,8,12);text="ROMsGames · External website. Select a game, then its download link to import."}
        root.addView(status)
        progress=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {max=100}
        root.addView(progress,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,4.dp()))
        web=WebView(this).apply {
            setBackgroundColor(Color.rgb(18,18,18))
            settings.javaScriptEnabled=true
            settings.domStorageEnabled=true
            settings.allowFileAccess=false
            settings.allowContentAccess=false
            settings.setSupportMultipleWindows(false)
            settings.mixedContentMode=WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.mediaPlaybackRequiresUserGesture=true
            settings.cacheMode=WebSettings.LOAD_DEFAULT
            isFocusable=true;isFocusableInTouchMode=true
            webViewClient=object:WebViewClient() {
                override fun shouldOverrideUrlLoading(view:WebView, request:WebResourceRequest):Boolean {
                    // Keep browsing on the requested provider. Ad schemes and unrelated popup pages stay out.
                    if(request.isForMainFrame && !providerPage(request.url) && !downloadLink(request.url)) {status.text="Blocked external page: ${request.url.host ?: "unknown host"}. Return to ROMsGames or your library.";return true}
                    return false
                }
                override fun onReceivedError(view:WebView,request:WebResourceRequest,error:WebResourceError) {
                    if(request.isForMainFrame) status.text="Could not open the library: ${error.description}. Reload or return to your offline games."
                }
                override fun onPageFinished(view:WebView,url:String) {if(transfer?.isActive!=true)status.text="${Uri.parse(url).host} · ${view.title ?: "Game library"}"}
            }
            webChromeClient=object:WebChromeClient() {override fun onProgressChanged(view:WebView,value:Int) {if(transfer?.isActive!=true)this@GameBrowserActivity.progress.progress=value}}
            setDownloadListener {url,agent,disposition,mime,_->
                if(transfer?.isActive==true) {status.text="One download is already running.";return@setDownloadListener}
                val name=URLUtil.guessFileName(url,disposition,mime)
                val ext=name.substringAfterLast('.').lowercase()
                if(ext !in CartridgeImporter.extensions && ext!="zip") {status.text="Unsupported download: $name. Choose a cartridge, ZIP or single-file PS1 ISO/PBP/EXE.";return@setDownloadListener}
                AlertDialog.Builder(this@GameBrowserActivity).setTitle("Add to your library?")
                    .setMessage("$name\n\nFrom ${Uri.parse(url).host}. The file will be checked and imported. ZIP files must contain one supported game.")
                    .setNegativeButton("Cancel",null).setPositiveButton("Download") {_,_->download(url,agent,name)}.show()
            }
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,false)
        root.addView(web,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))
        setContentView(root)
        onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true) {override fun handleOnBackPressed()=goBack()})
        web.loadUrl(intent.getStringExtra("url")?.takeIf {providerPage(Uri.parse(it))} ?: HOME)
    }
    private fun providerPage(uri:Uri)=uri.scheme=="https" && uri.host?.let {it=="romsgames.net" || it.endsWith(".romsgames.net")}==true
    private fun downloadLink(uri:Uri)=uri.scheme=="https" && (
        uri.lastPathSegment?.substringAfterLast('.')?.lowercase() in (CartridgeImporter.extensions + "zip") ||
        uri.host?.let { it=="downloadroms.io" || it.endsWith(".downloadroms.io") }==true || uri.getQueryParameter("mediaAttach")!=null
    )
    private fun Int.dp()=(this*resources.displayMetrics.density).toInt()
    private fun goBack() {if(web.canGoBack())web.goBack() else finish()}
    override fun onKeyUp(keyCode:Int,event:KeyEvent):Boolean {
        if(keyCode==KeyEvent.KEYCODE_ESCAPE) {goBack();return true}
        return super.onKeyUp(keyCode,event)
    }
    private fun download(address:String,agent:String,name:String) {
        // Cookies are read on the UI thread and sent only to their matching host.
        val initialCookies=CookieManager.getInstance().getCookie(address)
        transfer=lifecycleScope.launch {
            status.text="Downloading $name…";progress.isIndeterminate=true
            try {
                val file=withContext(Dispatchers.IO) {
                    val temp=File.createTempFile("browser-", ".download", cacheDir);pending=temp
                    try {
                        var next=URL(address)
                        var accepted:HttpURLConnection?=null
                        for (redirect in 0..5) {
                            ensureActive()
                            require(next.protocol=="https") {"Only HTTPS downloads are supported."}
                            val conn=next.openConnection() as HttpURLConnection
                            connection=conn;conn.instanceFollowRedirects=false;conn.connectTimeout=15000;conn.readTimeout=20000
                            conn.setRequestProperty("User-Agent",agent)
                            conn.setRequestProperty("Referer",HOME)
                            if(next.host.equals(URL(address).host,true) && initialCookies!=null)conn.setRequestProperty("Cookie",initialCookies)
                            val code=conn.responseCode
                            if(code in listOf(301,302,303,307,308)) {
                                val location=conn.getHeaderField("Location") ?: error("Download redirect has no destination.")
                                next=URL(next,location);conn.disconnect()
                            } else {
                                require(code in 200..299) {"Download server returned HTTP $code."}
                                accepted=conn;break
                            }
                        }
                        val conn=accepted ?: error("Too many download redirects.")
                        val length=conn.contentLengthLong
                        require(length<=CartridgeImporter.MAX_DOWNLOAD) {"This download exceeds 768 MiB."}
                        require(android.os.StatFs(filesDir.absolutePath).availableBytes>maxOf(length,32L*1024*1024)*2+32L*1024*1024) {"Free some storage before downloading this game."}
                        var count=0L;var reported=-1
                        conn.inputStream.use {input->temp.outputStream().use {output->
                            val buffer=ByteArray(65536)
                            while(true) {
                                ensureActive();val n=input.read(buffer);if(n<0)break;count+=n
                                require(count<=CartridgeImporter.MAX_DOWNLOAD) {"Download exceeds 768 MiB."}
                                output.write(buffer,0,n)
                                val percent=if(length>0)(count*100/length).toInt() else -1
                                if(percent!=reported && percent>=0) {reported=percent;withContext(Dispatchers.Main) {progress.isIndeterminate=false;progress.progress=percent;status.text="Downloading $name · $percent%"}}
                            }
                            output.fd.sync()
                        }}
                        require(length<0 || count==length) {"Download was interrupted. Please retry."}
                        conn.disconnect();connection=null;temp
                    } catch(e:Exception) {temp.delete();throw e}
                }
                status.text="Checking and importing $name…";progress.isIndeterminate=true
                val game=file.inputStream().use {CartridgeImporter(this@GameBrowserActivity,FireEmuApp.instance.gameRepository).import(it,name)}
                file.delete();pending=null
                status.text="${game.cleanTitle} is in your library. Select My library to play.";progress.progress=100
            } catch(e:CancellationException) {if(!destroyed)status.text="Download cancelled. Your library is unchanged.";throw e}
            catch(e:Exception) {status.text=e.message ?: "Could not download this game. Please retry."}
            finally {connection?.disconnect();connection=null;pending?.delete();pending=null;progress.isIndeterminate=false}
        }
    }
    override fun onPause() {web.onPause();super.onPause()}
    override fun onResume() {super.onResume();if(::web.isInitialized)web.onResume()}
    override fun onDestroy() {
        destroyed=true;transfer?.cancel();connection?.disconnect()
        if(::web.isInitialized) {(web.parent as? ViewGroup)?.removeView(web);web.stopLoading();web.loadUrl("about:blank");web.removeAllViews();web.destroy()}
        super.onDestroy()
    }
}
