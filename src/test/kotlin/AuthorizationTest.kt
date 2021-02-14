import TestHelper.LoadConfig
import TestHelper.MockHelper.createServer
import TestHelper.PathHelper.randomString
import com.seansoper.batil.connectors.etrade.Authorization
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

fun mockAuthResponse(): Triple<String, String, String> {
    val token = randomString()
    val secret = randomString()
    val content = "oauth_token=$token&oauth_token_secret=$secret"

    return Triple(token, secret, content)
}

class AuthorizationTest: StringSpec({
    val config = LoadConfig()

    "get request token" {
        val mock = mockAuthResponse()

        createServer(mock.third,"Content-Type" to "application/x-www-form-urlencoded") {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val data = service.getRequestToken()

            data.shouldNotBeNull()
            data.accessToken.shouldBe(mock.first)
            data.accessSecret.shouldBe(mock.second)

            it.takeRequest().path.shouldContain("oauth/request_token")
        }
    }

})