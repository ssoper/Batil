package com.seansoper.batil

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import java.nio.file.Files
import java.nio.file.Paths

data class Chromium(val ip: String,
                    val port: Int,
                    val delay: Int)

data class EtradeAuth(val key: String,
                      val secret: String)

data class EtradeConfiguration(val sandbox: EtradeAuth,
                               val production: EtradeAuth,
                               val username: String,
                               val password: String)

data class Configuration(val etrade: EtradeConfiguration,
                         val chromium: Chromium = Chromium("127.0.0.1", port = 9222, delay = 5))

class IngestConfiguration(private val settings: CommandLineParser.Parsed,
                          private val basePath: String = System.getProperty("user.dir")) {

    fun parse(): Configuration {
        val path = Paths.get(basePath, "batil.yaml")
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())

        return try {
            Files.newBufferedReader(path).use {
                mapper.readValue(it, Configuration::class.java)
            }
        } catch (exception: MissingKotlinParameterException) {
            if (settings.verbose) {
                throw ConfigFileInvalid(exception.message)
            } else {
                throw ConfigFileInvalid()
            }
        }
    }
}

class ConfigFileInvalid(message: String? = "Configuration file could not be loaded, ensure all fields have values"): Exception(message)