package com.fireemu.data.catalog

import android.content.Context
import android.os.Build
import android.os.Process
import com.fireemu.data.repository.GameRepository
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File
import java.util.Base64

/** The signed embedded catalog remains available offline; refresh is explicitly requested. */
class DownloadRepository(private val context:Context,private val games:GameRepository) {
    companion object {
        const val RELEASE="https://github.com/Nirusaki-Malaal/firespot-cores/releases/download/phase1-v1"
    }
    data class Transfer(val id:String="",val message:String="",val progress:Int=0,val busy:Boolean=false,val failed:Boolean=false)
    private val network=ReleaseDownloader()
    private val mutex=Mutex()
    private val publicKey=context.assets.open("catalog/public-key.der").use {it.readBytes()}
    private val cache=File(context.filesDir,"catalog-cache.json")
    private val _catalog=MutableStateFlow(loadCatalog())
    val catalog:StateFlow<Catalog> = _catalog
    private val _transfer=MutableStateFlow(Transfer())
    val transfer:StateFlow<Transfer> = _transfer
    val abi:String = Build.SUPPORTED_ABIS.firstOrNull {it in listOf("armeabi-v7a","arm64-v8a","x86_64") && it.contains("64")==Process.is64Bit()} ?: error("Unsupported device CPU.")
    private fun trusted(payload:ByteArray,signature:ByteArray):Catalog {CatalogSecurity.verifySignature(payload,signature,publicKey);return Catalog.parse(payload)}
    private fun loadCatalog():Catalog {
        val base=trusted(context.assets.open("catalog/catalog.json").use {it.readBytes()},context.assets.open("catalog/catalog.sig").use {it.readBytes()})
        return try {
            require(cache.length()<=2*1024*1024)
            val o=JSONObject(cache.readText());val newer=trusted(Base64.getDecoder().decode(o.getString("payload")),Base64.getDecoder().decode(o.getString("signature")))
            if(newer.revision>=base.revision)newer else base
        } catch(_:Exception) {base}
    }
    fun core(console:ConsoleType):CatalogCore? = _catalog.value.cores.firstOrNull {console.id in it.consoleIds}
    fun coreFile(core:CatalogCore):File {
        val asset=core.artifacts.firstOrNull {it.abi==abi} ?: error("${core.title} is not available for this CPU yet.")
        return File(context.filesDir,"cores/$abi/${core.id}/${asset.sha256}/${asset.file}")
    }
    fun installedCore(core:CatalogCore):Boolean = core.artifacts.firstOrNull {it.abi==abi}?.let {a->coreFile(core).let {it.isFile && it.length()==a.bytes}} ?: false
    suspend fun refresh()=operation("catalog","Refreshing catalog") {
        val payload=network.bytes("$RELEASE/catalog.json",1024*1024)
        val signature=network.bytes("$RELEASE/catalog.sig",1024)
        val next=trusted(payload,signature);require(next.revision>=_catalog.value.revision) {"An older catalog cannot replace this version."}
        val temp=File.createTempFile("catalog-",".tmp",context.filesDir)
        try {
            temp.writeText(JSONObject().put("payload",Base64.getEncoder().encodeToString(payload)).put("signature",Base64.getEncoder().encodeToString(signature)).toString())
            check(temp.renameTo(cache));_catalog.value=next
        } finally {temp.delete()}
        "Catalog is up to date."
    }
    suspend fun installCore(core:CatalogCore)=operation(core.id,"Downloading ${core.title}") {
        val asset=core.artifacts.firstOrNull {it.abi==abi} ?: error("This core is not available for your CPU.")
        val file=coreFile(core)
        val healthy=try {CatalogSecurity.verifyFile(file,asset.bytes,asset.sha256,abi);true} catch(_:Exception) {false}
        if(!healthy) network.download(asset,file) {n->_transfer.value=_transfer.value.copy(progress=n)}
        "${core.title} is ready."
    }
    suspend fun ensureCore(console:ConsoleType) {
        val core=core(console) ?: error("${console.shortName} support is not released for FireSpot yet.")
        installCore(core)
    }
    suspend fun installedGame(entry:CatalogGame):Game? = withContext(Dispatchers.IO) {
        val starterName=entry.asset.file
        val starter=File(context.filesDir,"games/starter/$starterName")
        games.getGameByPath(starter.absolutePath) ?: games.getGameByPath(gameFile(entry).absolutePath)
    }
    private fun gameFile(entry:CatalogGame)=File(context.filesDir,"games/downloads/${entry.asset.sha256}/${entry.asset.file}")
    suspend fun installGame(entry:CatalogGame):Game = operation(entry.id,"Downloading ${entry.title}") {
        installedGame(entry)?.let {return@operation it}
        val file=gameFile(entry)
        val healthy=try {CatalogSecurity.verifyFile(file,entry.asset.bytes,entry.asset.sha256);true} catch(_:Exception) {false}
        if(!healthy) network.download(entry.asset,file) {n->_transfer.value=_transfer.value.copy(progress=n)}
        // Match content hashes, not user-controlled server filenames, for repeat installs.
        val existing=games.getGameByPath(file.absolutePath)
        if(existing!=null)return@operation existing
        games.insertOrUpdateGames(listOf(Game(title=entry.title,cleanTitle=entry.title,romPath=file.absolutePath,consoleId=entry.consoleId,fileSizeBytes=entry.asset.bytes,genre="Homebrew",releaseYear=2026,maxPlayers=1,rating="",description=entry.description)))
        games.getGameByPath(file.absolutePath) ?: error("The downloaded game could not be added to your library.")
    }
    private suspend fun <T> operation(id:String,message:String,block:suspend ()->T):T=withContext(Dispatchers.IO) {
        mutex.withLock {
            _transfer.value=Transfer(id,message,busy=true)
            try {val result=block();_transfer.value=Transfer(id,"Ready",100);result}
            catch(e:CancellationException) {_transfer.value=Transfer(id,"Download cancelled. You can retry.");throw e}
            catch(e:Exception) {_transfer.value=Transfer(id,e.message?:"Download failed. Please retry.",failed=true);throw e}
        }
    }
}
