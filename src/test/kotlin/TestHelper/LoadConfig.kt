package TestHelper

import com.seansoper.batil.CommandLineParser
import com.seansoper.batil.Configuration
import com.seansoper.batil.IngestConfiguration
import java.nio.file.Paths

class LoadConfig {
    val content: Configuration

    init {
        val configPath = Paths.get(this.javaClass.classLoader.getResource("batil.test.yaml")!!.toURI())
        val parsed = CommandLineParser.Parsed(configPath, false, false)
        content = IngestConfiguration(parsed).parse()
    }
}