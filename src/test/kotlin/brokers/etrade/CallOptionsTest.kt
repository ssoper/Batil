import com.seansoper.batil.brokers.etrade.api.MarginLevel
import com.seansoper.batil.brokers.etrade.api.MessageType
import com.seansoper.batil.brokers.etrade.api.OptionLevel
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyCallOptionLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyCallOptionMarket
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellCallOptionLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellCallOptionMarket
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths
import java.util.GregorianCalendar

class CallOptionsTest : StringSpec({
    val accountIdKey = randomString(6)

    val virginGalactic = TestOption(
        "SPCE",
        2021,
        10,
        8
    )

    val apple = TestOption(
        "AAPL",
        2021,
        10,
        15
    )

    // TODO: Consider using Buy To Open (BTO) vs. just Buy
    "create preview to buy call option limit" {
        val path = Paths.get("brokers/etrade/orders/call_options/create_preview_buy_limit.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionLimit(virginGalactic.symbol, 0.95f, 30f, 1, virginGalactic.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val message = data.orders.first().messages!!.messages.first()
            message.description.shouldContain("You are about to place a marketable limit order")
            message.code.shouldBe(9011)
            message.type.shouldBe(MessageType.WARNING)

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(virginGalactic.symbol)
            product.expiry.shouldBe(GregorianCalendar(virginGalactic.year, virginGalactic.month, virginGalactic.day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27825.10f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell call option limit" {
        val path = Paths.get("brokers/etrade/orders/call_options/create_preview_sell_limit.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallOptionLimit(virginGalactic.symbol, 0.95f, 30f, 1, virginGalactic.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(virginGalactic.symbol)
            product.expiry.shouldBe(GregorianCalendar(virginGalactic.year, virginGalactic.month, virginGalactic.day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27825.10f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy call option market" {
        val path = Paths.get("brokers/etrade/orders/call_options/create_preview_buy_market.json")

        createServer(path) {
            val strike = 150f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionMarket(apple.symbol, 1f, 0.5f, strike, 1, apple.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            data.orders.first().messages.shouldBeNull()

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(apple.symbol)
            product.expiry.shouldBe(GregorianCalendar(apple.year, apple.month, apple.day))

            val osi = "${apple.symbol}--${apple.year - 2000}${apple.month}${apple.day}C00${strike.toInt()}000"
            product.productId!!.symbol.shouldBe(osi)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell call option market" {
        val path = Paths.get("brokers/etrade/orders/call_options/create_preview_sell_market.json")

        createServer(path) {
            val strike = 150f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallOptionMarket(apple.symbol, 1f, 0.5f, strike, 1, apple.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            data.orders.first().messages.shouldBeNull()

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(apple.symbol)
            product.expiry.shouldBe(GregorianCalendar(apple.year, apple.month, apple.day))

            val osi = "${apple.symbol}--${apple.year - 2000}${apple.month}${apple.day}C00${strike.toInt()}000"
            product.productId!!.symbol.shouldBe(osi)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
