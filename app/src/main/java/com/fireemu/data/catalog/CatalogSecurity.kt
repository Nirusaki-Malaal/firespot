package com.fireemu.data.catalog

import java.io.File
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/** JVM-only trust checks shared by the installer and its negative tests. */
object CatalogSecurity {
    fun verifySignature(payload: ByteArray, signature: ByteArray, publicKey: ByteArray) {
        require(payload.size <= 1024 * 1024) { "Catalog exceeds its size limit." }
        val key = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKey))
        require(Signature.getInstance("SHA256withRSA").run { initVerify(key); update(payload); verify(signature) }) { "Catalog signature is invalid." }
    }
    fun sha256(file: File): String = file.inputStream().use { input ->
        val digest=MessageDigest.getInstance("SHA-256");val buffer=ByteArray(65536)
        while(true) {val n=input.read(buffer);if(n<0)break;digest.update(buffer,0,n)}
        digest.digest().joinToString("") {"%02x".format(it)}
    }
    fun verifyFile(file:File, bytes:Long, hash:String, abi:String?=null) {
        require(file.length()==bytes && hash.matches(Regex("[a-f0-9]{64}")) && sha256(file)==hash) { "Download failed its integrity check. Please retry." }
        if(abi!=null) file.inputStream().use { input ->
            val header=ByteArray(20);require(input.read(header)==20) {"Core header is incomplete."}
            val expected=when(abi) {"armeabi-v7a"->1 to 40;"arm64-v8a"->2 to 183;"x86_64"->2 to 62;else->error("Unsupported CPU.")}
            require(header.take(4)==listOf<Byte>(127,69,76,70) && header[5].toInt()==1 && header[4].toInt()==expected.first && ((header[18].toInt() and 255) or ((header[19].toInt() and 255) shl 8))==expected.second) {"This core does not match your device CPU."}
        }
    }
}
