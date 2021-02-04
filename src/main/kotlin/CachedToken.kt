package com.seansoper.batil

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.util.*
import javax.crypto.spec.SecretKeySpec

// TODO: Handle java.io.IOException: Integrity check failed: java.security.UnrecoverableKeyException: Failed PKCS12 integrity checking

class CachedToken(val provider: Provider,
                  private val keyStorePath: Path = Paths.get(System.getProperty("user.home"), ".batil", "key.store"),
                  private val passwordPath: Path = Paths.get(System.getProperty("user.home"), ".batil", "key.password")) {

    enum class Provider {
        ETRADE
    }

    private val keyStore: KeyStore by lazy {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        if (keyStoreFile.exists()) {
            val stream = FileInputStream(keyStoreFile)
            keyStore.load(stream, password)
        } else {
            keyStore.load(null, password)
        }

        keyStore
    }

    private val keyStoreFile: File
        get() {
            val dirPath = keyStorePath.parent

            if (!dirPath.toFile().exists()) {
                dirPath.toFile().mkdirs()
            }

            return keyStorePath.toFile()
        }

    private val password: CharArray by lazy {
        val dirPath = passwordPath.parent
        if (!dirPath.toFile().exists()) {
            dirPath.toFile().mkdirs()
        }

        val path = passwordPath.toFile()

        if (path.exists()) {
            path.readText().toCharArray()
        } else {
            val uuid = UUID.randomUUID().toString()
            path.writeText(uuid)
            uuid.toCharArray()
        }
    }

    private fun getEntryKey(entry: String): String {
        return "${provider.name.toLowerCase()}.${entry.toLowerCase()}"
    }

    fun getEntry(entry: String): String? {
        if (!keyStoreFile.exists()) {
            return null
        }

        val protection = KeyStore.PasswordProtection(password)
        val secret = keyStore.getEntry(getEntryKey(entry), protection)

        // Can’t use optionals because getEntry is incorrectly marked as NonNullable
        return if (secret != null) {
            String((secret as KeyStore.SecretKeyEntry).secretKey.encoded)
        } else {
            null
        }
    }

    fun setEntry(entry: String, value: String) {
        val protection = KeyStore.PasswordProtection(password)
        val encoded = SecretKeySpec(value.toByteArray(), "AES")

        keyStore.setEntry(getEntryKey(entry), KeyStore.SecretKeyEntry(encoded), protection)

        val stream = FileOutputStream(keyStoreFile)
        stream.use {
            keyStore.store(it, password)
        }
    }

}