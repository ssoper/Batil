import com.seansoper.batil.CachedToken
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Path
import java.nio.file.Paths

fun randomString(length: Int = 15): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun tmpDirPath(): Path {
    return Paths.get("/tmp", "batil", randomString())
}

fun getPaths(): Triple<Path, Path, Path> {
    val dirPath = tmpDirPath()

    return Triple(dirPath,
                  Paths.get(dirPath.toString(), "key.store"),
                  Paths.get(dirPath.toString(), "key.password"))
}

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
})