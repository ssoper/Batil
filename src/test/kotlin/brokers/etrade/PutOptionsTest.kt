import com.seansoper.batil.brokers.etrade.api.MarginLevel
import com.seansoper.batil.brokers.etrade.api.MessageType
import com.seansoper.batil.brokers.etrade.api.OptionLevel
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyCallOptionLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyPutOptionMarket
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellCallOptionLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellPutOptionMarket
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

class PutOptionsTest : StringSpec({
    val accountIdKey = randomString(6)

    val amc = TestOption(
        "AMC",
        2021,
        10,
        15
    )

    val att = TestOption(
        "T",
        2021,
        10,
        15
    )

    "create preview to buy put option limit" {
        val path = Paths.get("brokers/etrade/orders/put_options/create_preview_buy_limit.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionLimit(amc.symbol, 5f, 35f, 1, amc.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val message = data.orders.first().messages!!.messages.first()
            message.description.shouldContain("You are about to place a marketable limit order")
            message.code.shouldBe(9011)
            message.type.shouldBe(MessageType.WARNING)

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(amc.symbol)
            product.expiry.shouldBe(GregorianCalendar(amc.year, amc.month, amc.day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27824.6f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell put option limit" {
        val path = Paths.get("brokers/etrade/orders/put_options/create_preview_sell_limit.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallOptionLimit(att.symbol, 0.65f, 27f, 1, att.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(att.symbol)
            product.expiry.shouldBe(GregorianCalendar(att.year, att.month, att.day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27824.6f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy call option market" {
        val path = Paths.get("brokers/etrade/orders/put_options/create_preview_buy_market.json")

        createServer(path) {
            val strike = 27f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyPutOptionMarket(att.symbol, .65f, 0f, strike, 1)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.priceType.shouldBe(OrderPriceType.MARKET)

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(att.symbol)
            product.expiry.shouldBe(GregorianCalendar(att.year, att.month, att.day))

            val osi = "${att.symbol}-----${att.year - 2000}${att.month}${att.day}P000${strike.toInt()}000"
            product.product!!.symbol.shouldBe(osi)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell put option market" {
        val path = Paths.get("brokers/etrade/orders/put_options/create_preview_sell_market.json")

        createServer(path) {
            val strike = 27f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellPutOptionMarket(att.symbol, .65f, 0f, strike, 1)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.priceType.shouldBe(OrderPriceType.MARKET)

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(att.symbol)
            product.expiry.shouldBe(GregorianCalendar(att.year, att.month, att.day))

            val osi = "${att.symbol}-----${att.year - 2000}${att.month}${att.day}P000${strike.toInt()}000"
            product.product!!.symbol.shouldBe(osi)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

})
