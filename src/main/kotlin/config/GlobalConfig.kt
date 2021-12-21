package com.seansoper.batil.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import java.nio.file.Files

// TODO: Consider modifying the parse method to just take a path and showing the Jackson error regardless of verbose
// TODO: Add a constructor that takes a ClientConfig
// TODO: Add options to take values as is or path to file, use live endpoint
// TODO: Session should expose high level APIs
// TODO: No more passing verbose/production thru every call

val DefaultChromium = Chromium("127.0.0.1", port = 9222, delay = 5)

data class GlobalConfig(
    val etrade: EtradeConfig,
    val chromium: Chromium = DefaultChromium
) {
    companion object {
        fun parse(clientConfig: ClientConfig): GlobalConfig {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())

            return try {
                Files.newBufferedReader(clientConfig.pathToConfigFile).use {
                    mapper.readValue(it, GlobalConfig::class.java)
                }
            } catch (exception: MissingKotlinParameterException) {
                if (clientConfig.verbose) {
                    throw ConfigFileInvalid(exception.message)
                } else {
                    throw ConfigFileInvalid()
                }
            }
        }
    }
}

data class Chromium(
    val ip: String,
    val port: Int,
    val delay: Int
)

/**
 * @suppress
 */
class ConfigFileInvalid(message: String? = "Error loading configuration file, ensure all fields have values") : Exception(message)

/**
 * @suppress
 */
class ConfigFileNotFound : Exception("Configuration file not found")
