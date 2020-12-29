import TestHelper.LoadConfig
import TestHelper.MockResponseFile
import com.seansoper.batil.connectors.Etrade
import com.seansoper.batil.connectors.EtradeAuthResponse
import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class EtradeTest: StringSpec({
    val config = LoadConfig().content

    "single ticker" {
        val server = MockWebServer()
        server.start()

        val content = MockResponseFile("apiResponses/market/quote/success.json").content
        content.shouldNotBeNull()

        val response = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(content)
        server.enqueue(response)

        val client = Etrade(config, baseUrl = server.url(".").toString())
        val oauth = EtradeAuthResponse("token", "secret")
        val data = client.ticker("AAPL", oauth, "verifierCode") // server.hostName

        data.shouldNotBeNull()
        server.takeRequest().path.shouldBe("/v1/market/quote/AAPL")
    }

})