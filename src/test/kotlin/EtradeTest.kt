import TestHelper.LoadConfig
import TestHelper.MockResponseFile
import com.seansoper.batil.connectors.Etrade
import com.seansoper.batil.connectors.EtradeAuthResponse
import com.seansoper.batil.connectors.OptionCategory
import com.seansoper.batil.connectors.OptionType
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.*

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

    "option chain" {
        createServer("apiResponses/market/option_chains/nearest_expiry_all_strikes_success.json") {
            val client = Etrade(config, baseUrl = it.url(".").toString())
            val oauth = EtradeAuthResponse("token", "secret")
            val data = client.optionChains("AAPL", oauth, "verifierCode")

            data.shouldNotBeNull()
            data.pairs.size.shouldBe(72)

            val pair = data.pairs.elementAt(0)
            pair.call.symbol.shouldBe("AAPL")
            pair.call.optionCategory.shouldBe(OptionCategory.STANDARD)
            pair.call.optionType.shouldBe(OptionType.CALL)
            pair.call.strikePrice.shouldBe(65.0f)
            pair.call.bid.shouldBe(69.8f)
            pair.call.ask.shouldBe(74.0f)
            pair.call.greeks.vega.shouldBe(0.0014f)

            pair.put.symbol.shouldBe("AAPL")
            pair.put.optionCategory.shouldBe(OptionCategory.STANDARD)
            pair.put.optionType.shouldBe(OptionType.PUT)
            pair.put.strikePrice.shouldBe(65.0f)
            pair.put.bid.shouldBe(0.0f)
            pair.put.ask.shouldBe(0.01f)
            pair.put.greeks.vega.shouldBe(0.0001f)

            it.takeRequest().path.shouldBe("/v1/market/optionchains?symbol=AAPL")
        }
    }

    "option chain specific expiry strike" {
        createServer("apiResponses/market/option_chains/specific_expiry_strike_distance_success.json") {
            val client = Etrade(config, baseUrl = it.url(".").toString())
            val oauth = EtradeAuthResponse("token", "secret")
            val data = client.optionChains("AAPL", GregorianCalendar(2021, 2, 5), 131f, 1, oauth, "verifierCode")

            data.shouldNotBeNull()
            data.pairs.size.shouldBe(3)

            val first = data.pairs.elementAt(0)
            first.call.symbol.shouldBe("AAPL")
            first.call.strikePrice.shouldBe(130.0f)

            val second = data.pairs.elementAt(1)
            second.call.symbol.shouldBe("AAPL")
            second.call.strikePrice.shouldBe(131.0f)

            val third = data.pairs.elementAt(2)
            third.call.symbol.shouldBe("AAPL")
            third.call.strikePrice.shouldBe(132.0f)

            it.takeRequest().path.shouldBe("/v1/market/optionchains?symbol=AAPL&expiryYear=2021&expiryMonth=2&expiryDay=5&strikePriceNear=131&noOfStrikes=3")
        }
    }
})


