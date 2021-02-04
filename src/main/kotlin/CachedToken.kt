package com.seansoper.batil

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

// TODO: Handle java.io.IOException: Integrity check failed: java.security.UnrecoverableKeyException: Failed PKCS12 integrity checking

class CachedToken(val provider: Provider,
                  private val keyStorePath: Path = Paths.get(System.getProperty("user.home"), ".batil", "key.store"),
                  private val passwordPath: Path = Paths.get(System.getProperty("user.home"), ".batil", "key.password")) {

    enum class Provider {
        ETRADE
    }

    fun getEntry(entry: String): String? {
        val file = entryFile(entry)
        if (!file.exists()) {
            return null
        }

        val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val content = file.readText()

        return String(cipher.doFinal(Base64.getDecoder().decode(content)))
    }

    fun saveEntry(entry: String, value: String) {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray()))
        val stream = FileOutputStream(entryFile(entry))

        stream.use {
            stream.write(encrypted.toByteArray())
        }
    }

    private fun entryFile(entry: String): File {
        val name = "${provider.name.toLowerCase()}.${entry.toLowerCase()}"
        return Paths.get(keyStorePath.parent.toString(), name).toFile()
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

    private val secretKey: SecretKey by lazy {
        getProviderSecretKey() ?: setProviderSecretKey()
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

    private fun getProviderSecretKey(): SecretKey? {
        if (!keyStoreFile.exists()) {
            return null
        }

        val protection = KeyStore.PasswordProtection(password)
        val secret = keyStore.getEntry(provider.name.toLowerCase(), protection)

        // Can’t use optionals because getEntry is incorrectly marked as NonNullable
        return if (secret != null) {
            (secret as KeyStore.SecretKeyEntry).secretKey
        } else {
            null
        }
    }

    private fun setProviderSecretKey(): SecretKey {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val protection = KeyStore.PasswordProtection(password)
        val entry = KeyStore.SecretKeyEntry(secretKey)
        keyStore.setEntry(provider.name.toLowerCase(), entry, protection)

        val stream = FileOutputStream(keyStoreFile)
        stream.use {
            keyStore.store(it, password)
        }

        return secretKey
    }

}