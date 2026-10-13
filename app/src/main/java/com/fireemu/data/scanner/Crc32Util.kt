package com.fireemu.data.scanner

import java.io.File
import java.io.FileInputStream
import java.util.zip.CRC32

object Crc32Util {
    // For Fire TV RAM and I/O efficiency, read up to 4MB max for fast hashing
    private const val MAX_HASH_BYTES = 4 * 1024 * 1024L

    fun calculate(file: File): String {
        if (!file.exists() || !file.canRead()) return ""
        val crc = CRC32()
        val buffer = ByteArray(8192)
        var totalRead = 0L

        try {
            FileInputStream(file).use { input ->
                while (totalRead < MAX_HASH_BYTES) {
                    val toRead = Math.min(buffer.size.toLong(), MAX_HASH_BYTES - totalRead).toInt()
                    val bytesRead = input.read(buffer, 0, toRead)
                    if (bytesRead == -1) break
                    crc.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
            }
            return "%08X".format(crc.value)
        } catch (e: Exception) {
            return ""
        }
    }
}
