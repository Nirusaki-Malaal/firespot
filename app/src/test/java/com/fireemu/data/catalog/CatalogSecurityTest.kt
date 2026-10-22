package com.fireemu.data.catalog

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.net.URL
import java.security.KeyPairGenerator
import java.security.Signature

class CatalogSecurityTest {
    @Test fun signedCatalogRejectsTamperingAndWrongPublisher() {
        val pair=KeyPairGenerator.getInstance("RSA").apply {initialize(2048)}.generateKeyPair()
        val data="{\"schema\":1}".toByteArray()
        val signature=Signature.getInstance("SHA256withRSA").run {initSign(pair.private);update(data);sign()}
        CatalogSecurity.verifySignature(data,signature,pair.public.encoded)
        assertThrows(IllegalArgumentException::class.java) {CatalogSecurity.verifySignature(data+1,signature,pair.public.encoded)}
        val other=KeyPairGenerator.getInstance("RSA").apply {initialize(2048)}.generateKeyPair()
        assertThrows(IllegalArgumentException::class.java) {CatalogSecurity.verifySignature(data,signature,other.public.encoded)}
    }
    @Test fun validHashDoesNotHideWrongCpuOrTruncation() {
        val file=File.createTempFile("core-",".so")
        try {
            val bytes=ByteArray(32);bytes[0]=127;bytes[1]=69;bytes[2]=76;bytes[3]=70;bytes[4]=2;bytes[5]=1;bytes[18]=62;file.writeBytes(bytes)
            val hash=CatalogSecurity.sha256(file)
            CatalogSecurity.verifyFile(file,32,hash,"x86_64")
            assertThrows(IllegalArgumentException::class.java) {CatalogSecurity.verifyFile(file,32,hash,"arm64-v8a")}
            file.appendBytes(byteArrayOf(1))
            assertThrows(IllegalArgumentException::class.java) {CatalogSecurity.verifyFile(file,32,hash,"x86_64")}
        } finally {file.delete()}
    }
    @Test fun redirectsCannotEscapeToHttpOrAnUntrustedHost() {
        assertTrue(ReleaseDownloader.permitted(URL("https://release-assets.githubusercontent.com/a")))
        assertFalse(ReleaseDownloader.permitted(URL("http://github.com/release")))
        assertFalse(ReleaseDownloader.permitted(URL("https://github.com.evil.example/release")))
        assertFalse(ReleaseDownloader.permitted(URL("https://user@github.com/release")))
        assertFalse(ReleaseDownloader.permitted(URL("https://github.com:8443/release")))
    }
}
