package com.fireemu.data.catalog

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** HTTPS only; GitHub redirects are followed explicitly without forwarding credentials. */
class ReleaseDownloader {
    companion object {
        fun permitted(url:URL):Boolean = url.protocol=="https" && url.userInfo==null && (url.port==-1 || url.port==443) &&
            (url.host=="github.com" || url.host=="raw.githubusercontent.com" || url.host=="release-assets.githubusercontent.com" || url.host=="objects.githubusercontent.com")
    }
    private fun open(address:String):HttpURLConnection {
        var url=URL(address)
        repeat(6) {
            require(permitted(url)) {"Download address is not a trusted HTTPS release host."}
            val c=url.openConnection() as HttpURLConnection
            c.instanceFollowRedirects=false;c.connectTimeout=15000;c.readTimeout=15000;c.setRequestProperty("User-Agent","FireSpot/Phase1")
            val code=c.responseCode
            if(code in listOf(301,302,303,307,308)) {
                val location=c.getHeaderField("Location") ?: run {c.disconnect();error("Release redirect is missing its destination.")}
                val next=URL(url,location);c.disconnect();url=next
            } else {
                if(code!=200) {c.disconnect();error("Download server returned $code. Please retry later.")}
                return c
            }
        }
        error("Too many download redirects.")
    }
    suspend fun bytes(url:String,max:Int):ByteArray {
        val c=open(url)
        try {return c.inputStream.use { input ->
            val out=java.io.ByteArrayOutputStream();val buf=ByteArray(16384)
            while(true) {currentCoroutineContext().ensureActive();val n=input.read(buf);if(n<0)break;require(out.size()+n<=max) {"Response exceeds its size limit."};out.write(buf,0,n)}
            out.toByteArray()
        }} finally {c.disconnect()}
    }
    suspend fun download(asset:DownloadAsset,destination:File,progress:(Int)->Unit) {
        destination.parentFile!!.mkdirs()
        require(destination.parentFile!!.usableSpace>asset.bytes+1024*1024) {"Not enough free space. Free some storage and retry."}
        val temp=File.createTempFile("download-",".part",destination.parentFile)
        val c=open(asset.url)
        try {
            var total=0L
            c.inputStream.use {input->FileOutputStream(temp).use {output->
                val buf=ByteArray(65536)
                while(true) {currentCoroutineContext().ensureActive();val n=input.read(buf);if(n<0)break;total+=n;require(total<=asset.bytes) {"Download exceeds its published size."};output.write(buf,0,n);progress((total*100/asset.bytes).toInt())}
                output.flush();output.fd.sync()
            }}
            CatalogSecurity.verifyFile(temp,asset.bytes,asset.sha256,asset.abi)
            currentCoroutineContext().ensureActive()
            // Same-directory rename preserves the previous installed artifact on failure.
            check(temp.renameTo(destination)) {"Could not finish installing the download."}
        } finally {c.disconnect();temp.delete()}
    }
}
