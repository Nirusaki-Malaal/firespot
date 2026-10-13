package com.fireemu.data.repository

import android.content.Context
import com.fireemu.domain.models.ConsoleType
import java.io.File

class CoreRepositoryImpl(
    private val context: Context
) : CoreRepository {

    override fun getAvailableConsoles(): List<ConsoleType> {
        return ConsoleType.values().toList()
    }

    override fun getCorePath(console: ConsoleType): String {
        val nativeDir = context.applicationInfo.nativeLibraryDir
        return File(nativeDir, console.coreLibName).absolutePath
    }

    override fun isCoreAvailable(console: ConsoleType): Boolean {
        val path = getCorePath(console)
        return File(path).exists()
    }

    override fun getBiosDirectory(): String {
        val defaultBios = File("/sdcard/ROMs/bios")
        if (!defaultBios.exists()) {
            defaultBios.mkdirs()
        }
        return defaultBios.absolutePath
    }
}
