package com.seansoper.batil.config

import java.nio.file.Path
import java.nio.file.Paths

data class ClientConfig(
    val pathToConfigFile: Path,
    val verbose: Boolean,
    val production: Boolean,
) {
    companion object {
        fun default(): ClientConfig = ClientConfig(
            pathToConfigFile = Paths.get(System.getProperty("user.dir"), "batil.yaml"),
            verbose = false,
            production = false
        )
    }
}
