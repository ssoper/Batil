import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.seansoper.batil.CommandLineParser
import com.seansoper.batil.IngestConfiguration
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class IngestConfigurationTest: StringSpec({

    "success" {
        val path = Paths.get(this.javaClass.classLoader.getResource("batil.test.yaml")!!.toURI())
        val parsed = CommandLineParser.Parsed(path, false, false)
        val config = IngestConfiguration(parsed).parse()

        config.shouldNotBeNull()
        config.etrade.sandbox.key.shouldBe("sandboxKey")
        config.etrade.username.shouldBe("testUsername")
        config.etrade.production.key.shouldBe("prodKey")
    }

    "failure" {
        val path = Paths.get(this.javaClass.classLoader.getResource("failure.yaml")!!.toURI())
        val parsed = CommandLineParser.Parsed(path, false, false)
        val exception = shouldThrow<MismatchedInputException> {
            IngestConfiguration(parsed).parse()
        }

        exception.shouldNotBeNull()
    }
})