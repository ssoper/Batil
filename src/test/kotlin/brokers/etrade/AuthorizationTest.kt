
import com.seansoper.batil.CachedTokenProvider
import com.seansoper.batil.brokers.etrade.auth.AuthResponse
import com.seansoper.batil.brokers.etrade.auth.AuthResponseError
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.services.ExpiredTokenError
import com.seansoper.batil.brokers.etrade.services.Market
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.spyk
import testHelper.LoadConfig
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import java.nio.file.Paths

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

internal class MockTokenProvider : CachedTokenProvider {
    val tokens = Triple(randomString(), randomString(), randomString())

    override fun getEntry(entry: String): String {
        return when (entry) {
            "token" -> tokens.first
            "secret" -> tokens.second
            else -> tokens.third
        }
    }

    override fun setEntry(entry: String, value: String) {
        // nothing
    }

    override fun destroy() {
        // nothing
    }
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

    "invalid response" {
        val response = "<message>invalid response</message>"

        createServer(response, "Content-Type" to "application/x-www-form-urlencoded") {
            val service = Authorization(config.content, baseUrl = it.url(".").toString())
            val requestToken = mockAuthResponse()
            val verifier = randomString(6)
            val exception = shouldThrow<AuthResponseError> {
                service.getAccessToken(requestToken, verifier)
            }

            exception.message.shouldBe("No token returned")

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

    "create session" {
        val requestToken = mockAuthResponse()
        val accessToken = mockAuthResponse()
        val verifier = randomString(6)
        val authorization = spyk(Authorization(config.content))

        every { authorization.getRequestToken() } returns requestToken
        every { authorization.getVerifierCode(requestToken.accessToken) } returns verifier
        every { authorization.getAccessToken(requestToken, verifier) } returns accessToken

        val session = authorization.createSession()
        session.accessToken.shouldBe(accessToken.accessToken)
        session.accessSecret.shouldBe(accessToken.accessSecret)
    }

    "renew session" {
        val tokenStore = MockTokenProvider()
        val authorization = spyk(Authorization(config.content, tokenStore = tokenStore))
        every { authorization.renewAccessToken(any()) } returns true

        authorization.renewSession()!!.let {
            it.accessToken.shouldBe(tokenStore.tokens.first)
            it.accessSecret.shouldBe(tokenStore.tokens.second)
        }
    }

    "token expired" {
        val path = Paths.get("brokers/etrade/token_expired.xml")

        createServer(path, "Content-Type" to "application/xml", 400) {
            val service = Market(mockSession(), baseUrl = it.url(".").toString())
            shouldThrow<ExpiredTokenError> {
                service.lookup("AAPL")
            }
        }
    }
})
