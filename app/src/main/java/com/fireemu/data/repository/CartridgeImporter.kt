package com.fireemu.data.repository

import android.content.Context
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import kotlin.coroutines.coroutineContext

/** One import path for documents and browser downloads. Never trusts a server filename as a path. */
class CartridgeImporter(private val context: Context, private val repository: GameRepository) {
    companion object {
        val extensions = setOf("gb", "gbc", "nes", "gba", "sfc", "smc", "sms", "gg", "sg", "pbp", "iso", "exe")
        const val MAX_DOWNLOAD = 768L * 1024 * 1024
    }
    suspend fun import(input: InputStream, displayName: String): Game = withContext(Dispatchers.IO) {
        val name = displayName.substringAfterLast('/').substringAfterLast('\\').take(180)
        if (name.substringAfterLast('.').lowercase() == "zip") {
            ZipInputStream(input).use { zip ->
                var result: Game? = null
                var scanned = 0L
                var entries = 0
                while (true) {
                    coroutineContext.ensureActive()
                    val entry = zip.nextEntry ?: break
                    require(++entries <= 128) { "This archive has too many files. Import one game at a time." }
                    val ext = entry.name.substringAfterLast('.').lowercase()
                    if (!entry.isDirectory && ext in extensions) {
                        require(result == null) { "This ZIP contains several games. Extract it and choose one cartridge." }
                        // Stage the archive's single supported game; do not create database entries until fully scanned.
                        val pending = File.createTempFile("archive-", ".$ext", context.cacheDir)
                        try {
                            copyBounded(zip, pending, limit(ext))
                            // More supported entries must be detected before committing this file.
                            while (true) {
                                val next = zip.nextEntry ?: break
                                require(++entries <= 128) { "This archive has too many files." }
                                require(next.isDirectory || next.name.substringAfterLast('.').lowercase() !in extensions) { "This ZIP contains several games. Extract it and choose one cartridge." }
                                val buffer = ByteArray(32768)
                                while (true) { coroutineContext.ensureActive(); val count=zip.read(buffer); if(count<0) break; scanned+=count; require(scanned<=32L*1024*1024) {"Archive extras exceed the import limit."} }
                            }
                            result = pending.inputStream().use { importRaw(it, entry.name.substringAfterLast('/')) }
                        } finally { pending.delete() }
                        break
                    } else {
                        val buffer=ByteArray(32768)
                        while (true) { coroutineContext.ensureActive(); val count=zip.read(buffer); if(count<0) break; scanned+=count; require(scanned<=32L*1024*1024) {"Archive extras exceed the import limit."} }
                    }
                }
                result ?: error("No supported game found in this ZIP. Choose a cartridge or single-file PlayStation image.")
            }
        } else input.use { importRaw(it, name) }
    }
    private fun limit(ext: String) = when(ext) {
        "gb", "gbc" -> 8L*1024*1024
        "iso", "pbp" -> MAX_DOWNLOAD
        "exe" -> 4L*1024*1024
        else -> 32L*1024*1024
    }
    private suspend fun copyBounded(input: InputStream, target: File, limit: Long) {
        var total=0L
        target.outputStream().use { output ->
            val buffer=ByteArray(65536)
            while(true) { coroutineContext.ensureActive(); val n=input.read(buffer); if(n<0) break;total+=n;require(total<=limit) {"Game exceeds the ${limit/1024/1024} MiB import limit."};output.write(buffer,0,n) }
            output.fd.sync()
        }
    }
    private suspend fun importRaw(input: InputStream, name: String): Game {
        val ext=name.substringAfterLast('.').lowercase()
        val console=when(ext) {
            "gb","gbc" -> ConsoleType.GAMEBOY
            "nes" -> ConsoleType.NES
            "gba" -> ConsoleType.GBA
            "sfc","smc" -> ConsoleType.SNES
            "sms" -> ConsoleType.MASTERSYSTEM
            "gg" -> ConsoleType.GAMEGEAR
            "sg" -> ConsoleType.SG1000
            "iso","pbp","exe" -> ConsoleType.PS1
            else -> error("Unsupported file. Choose a cartridge, ZIP, or a single-file PlayStation ISO/PBP/EXE.")
        }
        val dir=File(context.filesDir,"games/imports").apply {check(mkdirs() || isDirectory)}
        val temp=File.createTempFile("import-", ".tmp", dir)
        try {
            copyBounded(input,temp,limit(ext))
            val total=temp.length()
            val header=ByteArray(512)
            val headerSize=temp.inputStream().use {it.read(header)}
            when(console) {
                ConsoleType.GAMEBOY -> require(total>=32768 && total%16384==0L) {"Incomplete Game Boy cartridge."}
                ConsoleType.NES -> require(header.take(4)==listOf<Byte>(78,69,83,26) && total>=16+(header[4].toInt() and 255)*16384L+(header[5].toInt() and 255)*8192L) {"Incomplete iNES cartridge."}
                ConsoleType.GBA -> require(total>=192) {"Incomplete GBA cartridge."}
                ConsoleType.SNES -> require(total>=32768 && (total%1024==0L || total%1024==512L)) {"Incomplete SNES cartridge."}
                ConsoleType.PS1 -> when(ext) {
                    "exe" -> require(headerSize>=8 && String(header,0,8,Charsets.US_ASCII)=="PS-X EXE") {"Not a PlayStation executable."}
                    "pbp" -> require(header.take(4)==listOf<Byte>(0,80,66,80)) {"Not a PBP image."}
                    else -> require(total>=32768 && (total%2048==0L || total%2352==0L)) {"Incomplete disc image."}
                }
                else -> require(total>=1024 && total%1024==0L) {"Incomplete Sega cartridge."}
            }
            val digest=MessageDigest.getInstance("SHA-256")
            temp.inputStream().use {stream->val buf=ByteArray(65536);while(true){coroutineContext.ensureActive();val n=stream.read(buf);if(n<0)break;digest.update(buf,0,n)}}
            val hash=digest.digest().joinToString("") {"%02x".format(it)}
            val file=File(dir,"$hash.$ext")
            val existing=repository.getGameByPath(file.absolutePath)
            if(!file.exists()) check(temp.renameTo(file)) {"Could not store imported game."}
            if(existing!=null) return existing
            val title=name.substringBeforeLast('.').take(160).ifBlank {"Imported game"}
            repository.insertOrUpdateGames(listOf(Game(title=title,cleanTitle=title,romPath=file.absolutePath,consoleId=console.id,fileSizeBytes=total,genre="Imported game",releaseYear=0,maxPlayers=1,rating="",description="Imported ${console.shortName} game. Open controls during play to review buttons and save progress.")))
            return repository.getGameByPath(file.absolutePath) ?: error("Could not add game to library.")
        } finally {temp.delete()}
    }
}
