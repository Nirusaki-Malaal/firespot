package com.fireemu.data.cloud

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CloudSaveArchiveTest {
    private fun workspace(block: (File) -> Unit) {
        val root = Files.createTempDirectory("firespot-cloud-test").toFile()
        try { block(root) } finally { root.deleteRecursively() }
    }
    @Test fun backupRestoresMissingAndKeepsNewerLocalProgress() = workspace { root ->
        val source = File(root, "source"); val target = File(root, "target")
        val save = File(source, "states/gba/test.state1").apply { parentFile!!.mkdirs(); writeText("older"); setLastModified(1600000000000) }
        File(source, "battery/gba/test.sav").apply { parentFile!!.mkdirs(); writeText("battery") }
        File(source, "roms/test.gba").apply { parentFile!!.mkdirs(); writeText("must not upload") }
        val zip = File(root, "backup.zip")
        assertEquals(2, CloudSaves.archive(source, zip))
        val local = File(target, save.relativeTo(source).path).apply { parentFile!!.mkdirs(); writeText("newer"); setLastModified(1700000000000) }
        val result = CloudSaves.restore(target, zip, File(root, "staging"))
        assertEquals(1 to 1, result)
        assertEquals("newer", local.readText())
        assertEquals("battery", File(target, "battery/gba/test.sav").readText())
        assertFalse(File(target, "roms").exists())
    }
    @Test fun invalidArchiveNeverMutatesExistingSaves() = workspace { root ->
        val target = File(root, "target")
        val local = File(target, "states/gb/game.state1").apply { parentFile!!.mkdirs(); writeText("original") }
        val zip = File(root, "evil.zip")
        ZipOutputStream(zip.outputStream()).use {
            it.putNextEntry(ZipEntry("states/gb/game.state1")); it.write("replacement".toByteArray()); it.closeEntry()
            it.putNextEntry(ZipEntry("states/../../escaped")); it.write(1); it.closeEntry()
        }
        try { CloudSaves.restore(target, zip, File(root, "staging")); fail("Traversal should fail") } catch (_: IllegalArgumentException) { }
        assertEquals("original", local.readText())
        assertFalse(File(root, "escaped").exists())
    }
    @Test fun noSavesCannotCreateSuccessfulBackup() = workspace { root ->
        try { CloudSaves.archive(File(root, "empty"), File(root, "empty.zip")); fail("Empty backup should fail") } catch (_: IllegalArgumentException) { }
    }
}
