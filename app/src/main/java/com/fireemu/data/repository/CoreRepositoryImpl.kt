package com.fireemu.data.repository

import android.content.Context
import android.os.Build
import android.os.Process
import com.fireemu.domain.models.ConsoleType
import java.io.File

class CoreRepositoryImpl(
    private val context: Context
) : CoreRepository {

    override fun getAvailableConsoles(): List<ConsoleType> {
        return ConsoleType.values().filter { isCoreAvailable(it) }
    }

    override fun getCorePath(console: ConsoleType): String {
        if (com.fireemu.BuildConfig.BUNDLED_CORE && console == ConsoleType.GAMEBOY) {
            return File(context.applicationInfo.nativeLibraryDir, console.coreLibName).absolutePath
        }
        val downloads = com.fireemu.FireEmuApp.instance.downloads
        downloads.core(console)?.let { core ->
            val file = downloads.coreFile(core)
            if (file.isFile) return file.absolutePath
        }
        val abi = Build.SUPPORTED_ABIS.first { it.contains("64") == Process.is64Bit() }
        return File(context.filesDir, "cores/$abi/${console.coreLibName}").absolutePath
    }

    override fun isCoreAvailable(console: ConsoleType): Boolean {
        val path = getCorePath(console)
        return File(path).let { it.isFile && it.length() > 0 }
    }

    override fun getBiosDirectory(): String {
        val defaultBios = File(context.filesDir, "bios")
        if (!defaultBios.exists()) {
            defaultBios.mkdirs()
        }
        return defaultBios.absolutePath
    }
}
