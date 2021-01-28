import TestHelper.LoadConfig
import TestHelper.MockResponseFile
import com.seansoper.batil.connectors.Etrade
import com.seansoper.batil.connectors.EtradeAuthResponse
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

fun createServer(pathToContent: String? = null, test: (server: MockWebServer) -> Unit) {
    val server = MockWebServer()
    server.start()

    pathToContent?.let {
        val content = MockResponseFile(it).content
        content.shouldNotBeNull()

        val response = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(content)
        server.enqueue(response)
    }

    test(server)
    server.close()
}

class EtradeTest: StringSpec({
    val config = LoadConfig().content

    "single ticker" {
        createServer("apiResponses/market/quote/single_ticker_success.json") {
            val client = Etrade(config, baseUrl = it.url(".").toString())
            val oauth = EtradeAuthResponse("token", "secret")
            val data = client.ticker("AAPL", oauth, "verifierCode")

            data.shouldNotBeNull()
            data.ahFlag.shouldBeFalse()
            data.tickerData.shouldNotBeNull()
            data.tickerData.symbolDescription.shouldBe("APPLE INC COM")
            data.tickerData.adjustedFlag!!.shouldBeFalse()
            data.tickerData.ask!!.shouldBe(132.31f)
            data.tickerData.bid!!.shouldBe(132.29f)
            it.takeRequest().path.shouldBe("/v1/market/quote/AAPL")
        }
    }

    "multiple tickers" {
        createServer("apiResponses/market/quote/multiple_tickers_success.json") {
            val client = Etrade(config, baseUrl = it.url(".").toString())
            val oauth = EtradeAuthResponse("token", "secret")
            val data = client.tickers(listOf("AAPL", "GME"), oauth, "verifierCode")

            data.shouldNotBeNull()
            data.size.shouldBe(2)
            data.first().tickerData.shouldNotBeNull()
            data.first().tickerData.symbolDescription.shouldBe("APPLE INC COM")
            data.last().tickerData.shouldNotBeNull()
            data.last().tickerData.symbolDescription.shouldBe("GAMESTOP CORP NEW CL A")
            it.takeRequest().path.shouldBe("/v1/market/quote/AAPL,GME")
        }
    }

    "lookup ticker" {
        createServer("apiResponses/market/lookup_ticker_success.json") {
            val client = Etrade(config, baseUrl = it.url(".").toString())
            val oauth = EtradeAuthResponse("token", "secret")
            val data = client.lookup("Game", oauth, "verifierCode")

            data.shouldNotBeNull()
            data.size.shouldBe(10)
            data.elementAt(6).symbol.shouldBe("GME")
            it.takeRequest().path.shouldBe("/v1/market/lookup/Game")
        }
    }
})


