package TestHelper

import config.GlobalConfig
import config.RuntimeConfig
import java.nio.file.Paths

class LoadConfig {
    val content: GlobalConfig

    init {
        val configPath = Paths.get(this.javaClass.classLoader.getResource("batil.test.yaml")!!.toURI())
        val runtimeConfig = RuntimeConfig(configPath, false, false)
        content = GlobalConfig.parse(runtimeConfig)
    }
}