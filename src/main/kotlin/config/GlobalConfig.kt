package com.seansoper.batil.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import java.nio.file.Files

// TODO: Consider modifying the parse method to just take a path and showing the Jackson error regardless of verbose

data class GlobalConfig(
    val etrade: EtradeConfig,
    val chromium: Chromium = Chromium("127.0.0.1", port = 9222, delay = 5)
) {
    companion object {
        fun parse(runtimeConfig: RuntimeConfig): GlobalConfig {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())

            return try {
                Files.newBufferedReader(runtimeConfig.pathToConfigFile).use {
                    mapper.readValue(it, GlobalConfig::class.java)
                }
            } catch (exception: MissingKotlinParameterException) {
                if (runtimeConfig.verbose) {
                    throw ConfigFileInvalid(exception.message)
                } else {
                    throw ConfigFileInvalid()
                }
            }
        }
    }
}

data class Chromium(val ip: String,
                    val port: Int,
                    val delay: Int)

class ConfigFileInvalid(message: String? = "Error loading configuration file, ensure all fields have values"): Exception(message)