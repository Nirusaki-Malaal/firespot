package com.fireemu.data.cloud

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object CloudSaves {
    private const val LIMIT = 32L * 1024 * 1024
    fun request(address: String, method: String = "POST", body: JSONObject? = null, token: String? = null, target: String? = null): JSONObject {
        val url = URL(address)
        require(url.protocol == "https" && url.userInfo == null && url.host.isNotBlank()) { "A valid HTTPS endpoint is required." }
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method; conn.connectTimeout = 8000; conn.readTimeout = 30000; conn.instanceFollowRedirects = false
            conn.setRequestProperty("Content-Type", if (target == null) "application/json" else "application/x-amz-json-1.1")
            if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")
            if (target != null) conn.setRequestProperty("X-Amz-Target", "AWSCognitoIdentityProviderService.$target")
            if (method != "GET") { conn.doOutput = true; conn.outputStream.use { it.write((body ?: JSONObject()).toString().toByteArray(Charsets.UTF_8)) } }
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val raw = stream?.use { input -> val out = java.io.ByteArrayOutputStream(); val buf = ByteArray(8192)
                while (true) { val n = input.read(buf); if (n < 0) break; require(out.size() + n <= 1024 * 1024) { "Response too large." }; out.write(buf, 0, n) }; out.toString("UTF-8") } ?: "{}"
            val json = JSONObject(raw)
            if (status !in 200..299) error(json.optString("error", json.optString("message", "Request failed ($status). Please sign in again if your session expired.")))
            return json
        } finally { conn.disconnect() }
    }

    fun cognito(region: String, client: String, operation: String, data: JSONObject): JSONObject {
        require(region.matches(Regex("[a-z]{2}(?:-[a-z]+)+-\\d"))) { "Check the AWS region." }
        require(client.matches(Regex("[a-zA-Z0-9]{10,128}"))) { "Check the Cognito client ID." }
        data.put("ClientId", client)
        return request("https://cognito-idp.$region.amazonaws.com/", body = data, target = operation)
    }

    fun archive(filesDir: File, output: File): Int {
        var count = 0; var total = 0L
        ZipOutputStream(output.outputStream().buffered()).use { zip ->
            for (folder in listOf("states", "battery")) {
                File(filesDir, folder).walkTopDown().filter { it.isFile && it.extension != "png" }.forEach { file ->
                    require(++count <= 2000) { "Too many save files for one backup." }
                    total += file.length(); require(total <= 64L * 1024 * 1024) { "Save files exceed 64 MiB." }
                    val entry = ZipEntry(file.relativeTo(filesDir).invariantSeparatorsPath); entry.time = file.lastModified()
                    zip.putNextEntry(entry); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
        }
        require(count > 0) { "No saves yet. Save progress in a game first." }
        require(output.length() <= LIMIT) { "Backup exceeds 32 MiB." }
        return count
    }

    /** Validate every entry before touching live saves; retain newer local progress. */
    fun restore(filesDir: File, archive: File, staging: File): Pair<Int, Int> {
        require(archive.length() <= LIMIT) { "Backup exceeds 32 MiB." }
        staging.deleteRecursively(); check(staging.mkdirs())
        var restored = 0; var skipped = 0; var total = 0L; var count = 0
        val entries = mutableListOf<Pair<File, Long>>()
        val seen = mutableSetOf<String>()
        try {
            ZipInputStream(archive.inputStream().buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(++count <= 2000) { "Too many backup entries." }
                    val name = entry.name
                    require(!entry.isDirectory && !name.contains('\\') && name.split('/').none { it == ".." || it.isEmpty() } && (name.startsWith("states/") || name.startsWith("battery/"))) { "Invalid backup path." }
                    require(seen.add(name)) { "Duplicate backup entry." }
                    val temp = File(staging, name)
                    require(temp.canonicalPath.startsWith(staging.canonicalPath + File.separator)) { "Invalid backup path." }
                    check(temp.parentFile!!.mkdirs() || temp.parentFile!!.isDirectory)
                    temp.outputStream().use { out ->
                        val buffer = ByteArray(8192)
                        while (true) { val n = zip.read(buffer); if (n < 0) break; total += n; require(total <= 64L * 1024 * 1024) { "Expanded backup exceeds 64 MiB." }; out.write(buffer, 0, n) }
                    }
                    entries.add(temp to entry.time)
                }
            }
            require(entries.isNotEmpty()) { "Backup contains no saves." }
            for ((temp, timestamp) in entries) {
                val dest = File(filesDir, temp.relativeTo(staging).path)
                require(dest.canonicalPath.startsWith(filesDir.canonicalPath + File.separator))
                if (dest.exists() && (timestamp <= 0 || dest.lastModified() >= timestamp)) { skipped++; continue }
                check(dest.parentFile!!.mkdirs() || dest.parentFile!!.isDirectory)
                // Same-filesystem rename replaces a save atomically.
                check(temp.renameTo(dest)) { "Could not restore a save. Check free space." }
                if (timestamp > 0) dest.setLastModified(timestamp)
                restored++
            }
            return restored to skipped
        } finally { staging.deleteRecursively() }
    }

    fun upload(post: JSONObject, file: File) {
        val conn = URL(post.getString("url")).openConnection() as HttpURLConnection
        require(URL(post.getString("url")).protocol == "https")
        val boundary = "FireSpot" + java.util.UUID.randomUUID().toString()
        try {
            conn.requestMethod = "POST"; conn.doOutput = true; conn.instanceFollowRedirects = false; conn.connectTimeout = 8000; conn.readTimeout = 60000
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            val fields = post.getJSONObject("fields")
            val header = buildString {
                fields.keys().forEach { key -> append("--$boundary\r\nContent-Disposition: form-data; name=\"$key\"\r\n\r\n${fields.getString(key)}\r\n") }
                append("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"saves.zip\"\r\nContent-Type: application/zip\r\n\r\n")
            }.toByteArray(Charsets.UTF_8)
            val footer = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(header.size.toLong() + file.length() + footer.size)
            conn.outputStream.buffered().use { out ->
                out.write(header); file.inputStream().use { it.copyTo(out) }; out.write(footer)
            }
            check(conn.responseCode in 200..299) { "Backup upload failed. Your local saves are unchanged." }
        } finally { conn.disconnect() }
    }

    fun download(address: String, dest: File) {
        require(URL(address).protocol == "https")
        val conn = URL(address).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = false; conn.connectTimeout = 8000; conn.readTimeout = 60000
            check(conn.responseCode == 200) { "Could not download backup. Refresh and try again." }
            conn.inputStream.use { input -> dest.outputStream().use { out ->
                val bytes = ByteArray(8192); var total = 0L
                while (true) { val n = input.read(bytes); if (n < 0) break; total += n; require(total <= LIMIT) { "Backup exceeds 32 MiB." }; out.write(bytes, 0, n) }
            } }
        } finally { conn.disconnect() }
    }
}
