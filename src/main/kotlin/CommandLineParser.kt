package com.seansoper.batil

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.io.File
import java.nio.file.Path

class CommandLineParser(private val args: Array<String>) {

    data class Parsed(val pathToConfigFile: Path,
                      val verbose: Boolean,
                      val production: Boolean)

    @Throws(ConfigFileNotFound::class)
    fun parse(): Parsed {
        val parser = ArgParser("Batil")
        val config by parser.option(ArgType.String, description = "Path to YAML configuration file").default("batil.yaml")
        val verbose by parser.option(ArgType.Boolean, description = "Show additional debugging output").default(false)
        val production by parser.option(ArgType.Boolean, description = "Use production endpoints, default is sandbox").default(false)
        parser.parse(args)

        val configFile = File(config)
        if (!configFile.exists()) {
            throw ConfigFileNotFound()
        }

        return Parsed(configFile.toPath(), verbose, production)
    }

}

class ConfigFileNotFound: Exception("Configuration file not found")