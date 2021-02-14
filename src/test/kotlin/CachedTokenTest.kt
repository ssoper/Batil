import TestHelper.PathHelper.getPaths
import TestHelper.PathHelper.randomString
import com.seansoper.batil.CachedToken
import com.seansoper.batil.CachedTokenException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class CachedTokenTest: StringSpec({

    "should store values securely" {
        val (dirPath, keyStorePath, passwordPath) = getPaths()
        val provider = CachedToken(CachedToken.Provider.ETRADE, keyStorePath, passwordPath)
        val secret = randomString(30)
        provider.setEntry("topsecret", secret)
        val decoded = provider.getEntry("topsecret")
        secret.shouldBe(decoded)
        dirPath.toFile().deleteRecursively()
    }

    "should delete key store files" {
        val (dirPath, keyStorePath, passwordPath) = getPaths()
        val provider = CachedToken(CachedToken.Provider.ETRADE, keyStorePath, passwordPath)
        val secret = randomString(30)
        provider.setEntry("topsecret", secret)
        provider.destroy()
        keyStorePath.toFile().exists().shouldBe(false)
        passwordPath.toFile().exists().shouldBe(false)

        dirPath.toFile().deleteRecursively()
    }

    "invalid password" {
        val (dirPath, keyStorePath, passwordPath) = getPaths()
        val provider = CachedToken(CachedToken.Provider.ETRADE, keyStorePath, passwordPath)
        val secret = randomString(30)
        provider.setEntry("topsecret", secret)

        val badPasswordPath = Paths.get(dirPath.toString(), "key.badpassword")
        val newProvider = CachedToken(CachedToken.Provider.ETRADE, keyStorePath, badPasswordPath)
        shouldThrow<CachedTokenException> {
            newProvider.getEntry("topsecret")
        }

        dirPath.toFile().deleteRecursively()
    }
})