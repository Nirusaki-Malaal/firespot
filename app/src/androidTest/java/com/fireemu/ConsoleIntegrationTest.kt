package com.fireemu

import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fireemu.data.repository.CartridgeImporter
import com.fireemu.nativebridge.LibretroBridge
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Run only against an explicitly selected developer device. Fixtures are separate test APK assets. */
@RunWith(AndroidJUnit4::class)
class ConsoleIntegrationTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val context=instrumentation.targetContext
    private val importer=CartridgeImporter(context,FireEmuApp.instance.gameRepository)
    private fun bytes(name:String)=instrumentation.context.assets.open(name).use {it.readBytes()}
    private fun zip(vararg names:String):ByteArray {
        val buffer=ByteArrayOutputStream()
        ZipOutputStream(buffer).use {out->names.forEach {name->out.putNextEntry(ZipEntry(name));out.write(bytes("input-colour.nes"));out.closeEntry()}}
        return buffer.toByteArray()
    }
    @Test fun archiveImportIsBoundedAndDeduplicated()=runBlocking {
        val first=importer.import(ByteArrayInputStream(zip("nested/Validation.nes")),"download.zip")
        try {
            val again=importer.import(ByteArrayInputStream(zip("different-name.nes")),"again.zip")
            assertEquals(first.id,again.id)
            try {importer.import(ByteArrayInputStream(zip("one.nes","two.nes")),"multiple.zip");fail("Multiple games accepted")}catch(_:IllegalArgumentException){}
            try {importer.import(ByteArrayInputStream(byteArrayOf(0,1,2)),"broken.nes");fail("Truncated cartridge accepted")}catch(_:IllegalArgumentException){}
            assertTrue(File(first.romPath).length()>16)
        } finally {FireEmuApp.instance.gameRepository.deleteGame(first.id)}
    }
    @Test fun shippedBridgeExercisesEveryPublishedSystem()=runBlocking {
        val rows=JSONArray()
        val handlerThread=HandlerThread("validation-frame-reader").apply {start()}
        val latest=AtomicLong(0);val videoFrames=AtomicLong(0)
        val reader=ImageReader.newInstance(512,384,PixelFormat.RGBA_8888,3)
        reader.setOnImageAvailableListener({r->r.acquireLatestImage()?.use {image->
            val plane=image.planes[0];val b=plane.buffer;var h=1125899906842597L
            // Exclude row padding; only sample actual displayed pixels.
            for(y in 0 until image.height step 3)for(x in 0 until image.width step 3) {
                val offset=y*plane.rowStride+x*plane.pixelStride
                h=h*31+(b.get(offset).toLong() and 255);h=h*31+(b.get(offset+1).toLong() and 255);h=h*31+(b.get(offset+2).toLong() and 255)
            }
            latest.set(h);videoFrames.incrementAndGet()
        }},Handler(handlerThread.looper))
        val cases=listOf(
            Triple("gameboy","orbit-collector.gb","gambatte"),Triple("gbc","colour-orbit.gbc","gambatte"),
            Triple("nes","input-colour.nes","fceumm"),Triple("gba","input-colour.gba","mgba"),
            Triple("snes","cputest-basic.sfc","bsnes2014"),Triple("mastersystem","input-colour.sms","gearsystem"),
            Triple("gamegear","input-colour.gg","gearsystem"),Triple("sg1000","input-colour.sg","gearsystem"),
            Triple("ps1","pad.exe","pcsx"))
        try {
            cases.forEach { (system,rom,core)->
                val game=importer.import(ByteArrayInputStream(bytes(rom)),"Validation-$system.${rom.substringAfterLast('.')}")
                val lib=File(context.cacheDir,"validation-$core.so").apply {writeBytes(bytes("$core.so"))}
                var sampleCount=0L;var nonzero=0L
                try {
                    assertTrue("$system core init",LibretroBridge.nativeInitCore(lib.absolutePath))
                    assertTrue("$system surface",LibretroBridge.nativeSetSurface(reader.surface,512,384))
                    LibretroBridge.setAudioCallback {samples,n->sampleCount+=n;nonzero+=samples.take(n).count {it.toInt()!=0}}
                    assertTrue("$system import/load",LibretroBridge.nativeLoadGame(game.romPath,File(context.filesDir,"bios").apply {mkdirs()}.absolutePath))
                    val framesBefore=videoFrames.get()
                    repeat(180) {LibretroBridge.nativeRunFrame(0)};Thread.sleep(80);val before=latest.get()
                    repeat(30) {LibretroBridge.nativeRunFrame(1 shl 7)};Thread.sleep(80);val inputChanged=before!=latest.get()
                    val state=File(context.cacheDir,"validation-$system.state")
                    assertTrue("$system save",LibretroBridge.nativeSaveState(state.absolutePath))
                    repeat(5) {LibretroBridge.nativeRunFrame(0)}
                    assertTrue("$system restore",LibretroBridge.nativeLoadState(state.absolutePath))
                    LibretroBridge.nativeReset();repeat(15) {LibretroBridge.nativeRunFrame(0)}
                    assertTrue("$system rendered video",videoFrames.get()>framesBefore)
                    assertTrue("$system sample callback",sampleCount>0)
                    val full=system !in listOf("snes","ps1")
                    if(full) {assertTrue("$system audible samples",nonzero>0);assertTrue("$system input changes screen",inputChanged)}
                    rows.put(JSONObject().put("system",system).put("import",true).put("videoFrames",videoFrames.get()-framesBefore).put("audioSamples",sampleCount).put("nonzeroAudio",nonzero).put("inputChangesVideo",inputChanged).put("saveRestore",true).put("reset",true).put("coverage",if(full) "Android diagnostic integration passed" else "Partial; gameplay acceptance pending"))
                    state.delete()
                } finally {
                    LibretroBridge.nativeDestroy();LibretroBridge.setAudioCallback(null);lib.delete()
                    FireEmuApp.instance.gameRepository.deleteGame(game.id)
                }
            }
        } finally {
            File(context.filesDir,"console-validation.json").writeText(JSONObject().put("scope","Local Android x86_64; diagnostic content, not Fire TV performance or broad game compatibility").put("results",rows).toString(2))
            reader.close();handlerThread.quitSafely()
        }
    }
}
