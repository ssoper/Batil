package com.seansoper.batil

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class CommandLineParser(private val args: Array<String>,
                        private val basePath: String = System.getProperty("user.dir")) {

    val shouldShowHelp: Boolean = args.any { it == "-help" }
    val verbose: Boolean = args.any { it == "-verbose" }
    val production: Boolean = args.any { it == "-production" }

    data class Parsed(val pathToConfigFile: Path,
                      val verbose: Boolean,
                      val production: Boolean)

    fun showHelp() {
        val str = """
            Arguments

                -help              Show documentation
                -verbose           Show debugging output
                -production        Use production endpoints, default is sandbox
                -config=path       Path to configuration file, default is ./batil.yaml
        """.trimIndent()
        println(str)
    }

    fun parse(): Parsed {
        val pathToConfigFile = getPath("config") ?: Paths.get(basePath,"batil.yaml")

        if (!File(pathToConfigFile.toString()).exists()) {
            throw ConfigFileNotFound()
        }

        return Parsed(pathToConfigFile, verbose, production)
    }

    private fun<T: Any> parseArguments(regex: Regex, transform: (String) -> T): List<T> {
        val match = fun (str: String): T? {
            return regex.find(str)?.let {
                if (it.groups.count() < 2) {
                    return null
                }

                return it.groups[1]?.let {
                    transform(it.value.removeSurrounding("\"").removeSurrounding("'"))
                }
            }
        }

        return args.mapNotNull(match)
    }

    private fun getPath(type: String): Path? {
        val regex = Regex("^-${type}=(.*)")

        return parseArguments(regex) {
            if (it.startsWith("/")) {
                Paths.get(it)
            } else {
                Paths.get(basePath, it)
            }
        }.firstOrNull()
    }

}

class ConfigFileNotFound: Exception("Configuration file not found")