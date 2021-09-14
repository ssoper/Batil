import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.seansoper.batil.config.GlobalConfig
import com.seansoper.batil.config.RuntimeConfig
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class GlobalConfigTest : StringSpec({

    "success" {
        val path = Paths.get(this.javaClass.classLoader.getResource("batil.test.yaml")!!.toURI())
        val runtimeConfig = RuntimeConfig(path, false, false)
        val config = GlobalConfig.parse(runtimeConfig)

        config.shouldNotBeNull()
        config.etrade.sandbox.key.shouldBe("sandboxKey")
        config.etrade.username.shouldBe("testUsername")
        config.etrade.production.key.shouldBe("prodKey")
    }

    "failure" {
        val path = Paths.get(this.javaClass.classLoader.getResource("failure.yaml")!!.toURI())
        val runtimeConfig = RuntimeConfig(path, false, false)
        val exception = shouldThrow<MismatchedInputException> {
            GlobalConfig.parse(runtimeConfig)
        }

        exception.shouldNotBeNull()
    }
})
