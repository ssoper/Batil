package com.seansoper.batil

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Core {
    @JvmStatic fun main(args: Array<String>) {
        val cli = CommandLineParser(args)

        if (cli.shouldShowHelp) {
            cli.showHelp()
            exitProcess(0)
        }

        val parsed = try {
            cli.parse()
        } catch (exception: ConfigFileNotFound) {
            println("❌ ${exception.localizedMessage}")
            cli.showHelp()
            exitProcess(1)
        }

        println("verbose set to ${parsed.verbose}")
        println("config path set to ${parsed.pathToConfigFile}")

        val path = Paths.get(System.getProperty("user.dir"), "batil.yaml")
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())

        Files.newBufferedReader(path).use {
            val stuff = mapper.readValue(it, Configuration::class.java)
            println(stuff)
        }
    }
}
