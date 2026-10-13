package com.fireemu.data.scanner

import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

class RomScanner {
    suspend fun scanFolders(folders: List<String>): List<Game> = withContext(Dispatchers.IO) {
        val foundGames = mutableListOf<Game>()
        val allFoldersToScan = mutableSetOf<File>()

        for (path in folders) {
            val root = File(path)
            if (root.exists() && root.isDirectory) {
                allFoldersToScan.add(root)
            }
        }

        // Auto-detect USB OTG storage mounts in /storage
        val storageRoot = File("/storage")
        if (storageRoot.exists() && storageRoot.isDirectory) {
            val mounts = storageRoot.listFiles()
            if (mounts != null) {
                for (mount in mounts) {
                    if (mount.isDirectory &&
                        !mount.name.equals("emulated", ignoreCase = true) &&
                        !mount.name.equals("self", ignoreCase = true)
                    ) {
                        val usbRomDir = File(mount, "ROMs")
                        if (usbRomDir.exists() && usbRomDir.isDirectory) {
                            allFoldersToScan.add(usbRomDir)
                        }
                    }
                }
            }
        }

        for (dir in allFoldersToScan) {
            scanDirectoryRecursively(dir, null, foundGames)
        }

        return@withContext foundGames.distinctBy { it.romPath }
    }

    private fun scanDirectoryRecursively(
        directory: File,
        inferredConsole: ConsoleType?,
        results: MutableList<Game>
    ) {
        val files = directory.listFiles() ?: return

        // Check if directory matches a console name
        val dirConsole = ConsoleType.values().firstOrNull {
            it.id.equals(directory.name, ignoreCase = true) ||
            it.shortName.equals(directory.name, ignoreCase = true)
        } ?: inferredConsole

        for (file in files) {
            if (file.isDirectory) {
                // Skip hidden folders and BIOS folder from game listing
                if (!file.name.startsWith(".") && !file.name.equals("bios", ignoreCase = true)) {
                    scanDirectoryRecursively(file, dirConsole, results)
                }
            } else if (file.isFile) {
                val game = parseGameFile(file, dirConsole)
                if (game != null) {
                    results.add(game)
                }
            }
        }
    }

    private fun parseGameFile(file: File, hintConsole: ConsoleType?): Game? {
        val ext = "." + file.extension.lowercase()

        // Handle .zip archives
        if (ext == ".zip") {
            try {
                ZipFile(file).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (!entry.isDirectory) {
                            val entryExt = "." + entry.name.substringAfterLast('.', "").lowercase()
                            val matchedConsole = ConsoleType.findByExtension(entryExt) ?: hintConsole
                            if (matchedConsole != null) {
                                val clean = TitleCleaner.clean(file.name)
                                val crc = Crc32Util.calculate(file)
                                val cover = CoverArtResolver.findCoverArt(file, matchedConsole.id)
                                val meta = GameMetadataProvider.resolve(clean, matchedConsole.id)

                                return Game(
                                    title = file.nameWithoutExtension,
                                    cleanTitle = clean,
                                    romPath = file.absolutePath,
                                    consoleId = matchedConsole.id,
                                    coverArtPath = cover,
                                    fileSizeBytes = file.length(),
                                    crc32 = crc,
                                    genre = meta.genre,
                                    releaseYear = meta.year,
                                    maxPlayers = meta.players,
                                    rating = meta.rating,
                                    description = meta.description
                                )
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {}
            return null
        }

        // Direct ROM file
        val matchedConsole = ConsoleType.findByExtension(ext) ?: hintConsole ?: return null
        val clean = TitleCleaner.clean(file.name)
        val crc = Crc32Util.calculate(file)
        val cover = CoverArtResolver.findCoverArt(file, matchedConsole.id)
        val meta = GameMetadataProvider.resolve(clean, matchedConsole.id)

        return Game(
            title = file.nameWithoutExtension,
            cleanTitle = clean,
            romPath = file.absolutePath,
            consoleId = matchedConsole.id,
            coverArtPath = cover,
            fileSizeBytes = file.length(),
            crc32 = crc,
            genre = meta.genre,
            releaseYear = meta.year,
            maxPlayers = meta.players,
            rating = meta.rating,
            description = meta.description
        )
    }
}
