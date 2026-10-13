package com.fireemu.data.scanner

import java.io.File

object CoverArtResolver {
    private val IMAGE_EXTENSIONS = listOf(".png", ".jpg", ".jpeg", ".webp")

    fun findCoverArt(romFile: File, consoleId: String): String? {
        val parentDir = romFile.parentFile ?: return null
        val baseName = romFile.nameWithoutExtension

        // 1. Same directory: <romName>.png, <romName>.jpg
        for (ext in IMAGE_EXTENSIONS) {
            val candidate = File(parentDir, "$baseName$ext")
            if (candidate.exists()) return candidate.absolutePath
        }

        // 2. Subfolder "covers": covers/<romName>.png
        val localCoversDir = File(parentDir, "covers")
        if (localCoversDir.exists() && localCoversDir.isDirectory) {
            for (ext in IMAGE_EXTENSIONS) {
                val candidate = File(localCoversDir, "$baseName$ext")
                if (candidate.exists()) return candidate.absolutePath
            }
        }

        // 3. FireSpot convention: /FireSpot/art/<consoleId>/<game>/poster.jpg
        val fireSpotDirs = listOf(
            File("/FireSpot/art/$consoleId/$baseName"),
            File("/sdcard/FireSpot/art/$consoleId/$baseName")
        )
        for (fDir in fireSpotDirs) {
            if (fDir.exists() && fDir.isDirectory) {
                for (name in listOf("poster", "cover", "backdrop")) {
                    for (ext in IMAGE_EXTENSIONS) {
                        val cand = File(fDir, "$name$ext")
                        if (cand.exists()) return cand.absolutePath
                    }
                }
            }
        }

        // 4. Global covers folder: /sdcard/ROMs/covers/<consoleId>/<romName>.png
        val globalCoversDir = File("/sdcard/ROMs/covers/$consoleId")
        if (globalCoversDir.exists() && globalCoversDir.isDirectory) {
            for (ext in IMAGE_EXTENSIONS) {
                val candidate = File(globalCoversDir, "$baseName$ext")
                if (candidate.exists()) return candidate.absolutePath
            }
        }

        return null
    }

    fun findBackdropArt(romFile: File, consoleId: String): String? {
        val baseName = romFile.nameWithoutExtension
        val fireSpotDirs = listOf(
            File("/FireSpot/art/$consoleId/$baseName"),
            File("/sdcard/FireSpot/art/$consoleId/$baseName")
        )
        for (fDir in fireSpotDirs) {
            if (fDir.exists() && fDir.isDirectory) {
                for (name in listOf("backdrop", "background", "screenshot", "fanart")) {
                    for (ext in IMAGE_EXTENSIONS) {
                        val cand = File(fDir, "$name$ext")
                        if (cand.exists()) return cand.absolutePath
                    }
                }
            }
        }
        return null
    }
}
