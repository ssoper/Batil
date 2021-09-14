import TestHelper.LoadConfig
import TestHelper.MockHelper.createServer
import TestHelper.PathHelper.randomString
import com.seansoper.batil.brokers.etrade.AuthResponse
import com.seansoper.batil.brokers.etrade.Authorization
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

fun mockTokens(): Triple<String, String, String> {
    val token = randomString()
    val secret = randomString()
    val content = "oauth_token=$token&oauth_token_secret=$secret"

    return Triple(token, secret, content)
}

fun mockAuthResponse(): AuthResponse {
    val token = randomString()
    val secret = randomString()

    return AuthResponse(accessToken = token, accessSecret = secret)
}

class AuthorizationTest : StringSpec({
    val config = LoadConfig()

    "get request token" {
        val mock = mockTokens()

        createServer(mock.third, "Content-Type" to "application/x-www-form-urlencoded") {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val data = service.getRequestToken()

            data.shouldNotBeNull()
            data.accessToken.shouldBe(mock.first)
            data.accessSecret.shouldBe(mock.second)

            it.takeRequest().path.shouldContain("oauth/request_token")
        }
    }

    "get access token" {
        val mock = mockTokens()

        createServer(mock.third, "Content-Type" to "application/x-www-form-urlencoded") {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val requestToken = mockAuthResponse()
            val verifier = randomString(6)
            val data = service.getAccessToken(requestToken, verifier)

            data.shouldNotBeNull()
            data.accessToken.shouldBe(mock.first)
            data.accessSecret.shouldBe(mock.second)

            it.takeRequest().path.shouldContain("oauth/access_token")
        }
    }

    "renew access token" {
        createServer("", "Content-Type" to "text/plain", 200) {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val requestToken = mockAuthResponse()
            val data = service.renewAccessToken(requestToken)

            data.shouldNotBeNull()
            data.shouldBeInstanceOf<Boolean>()
            data.shouldBe(true)

            it.takeRequest().path.shouldContain("oauth/renew_access_token")
        }
    }

    "revoke access token" {
        createServer("", "Content-Type" to "text/plain", 200) {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val requestToken = mockAuthResponse()
            val data = service.revokeAccessToken(requestToken)

            data.shouldNotBeNull()
            data.shouldBeInstanceOf<Boolean>()
            data.shouldBe(true)

            it.takeRequest().path.shouldContain("oauth/revoke_access_token")
        }
    }
})
