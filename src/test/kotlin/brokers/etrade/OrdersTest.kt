import com.seansoper.batil.brokers.etrade.api.MarginLevel
import com.seansoper.batil.brokers.etrade.api.MessageType
import com.seansoper.batil.brokers.etrade.api.OptionLevel
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.OrderStatus
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

class OrdersTest : StringSpec({
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

    "list orders" {
        val path = Paths.get("brokers/etrade/orders/list.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val data = service.list(accountIdKey)

            data.shouldNotBeNull()

            val order = data.orders!!.first()
            order.orderId.shouldBe(389)
            order.orderType.shouldBe(OrderType.OPTN)

            val details = order.orderDetail!!.first()
            details.placedTime.shouldBe(Instant.ofEpochMilli(1631886307542))
            details.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            details.status.shouldBe(OrderStatus.EXECUTED)

            val instrument = details.instrument!!.first()
            instrument.symbolDescription.shouldContain("SPCE")
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
            instrument.orderedQuantity.shouldBe(1.0f)
            instrument.filledQuantity.shouldBe(1.0f)

            val product = instrument.product!!
            product.symbol.shouldBe("SPCE")
            product.securityType.shouldBe(SecurityType.OPTN)
            product.callPut.shouldBe(OptionType.CALL)
            product.expiry.shouldBe(GregorianCalendar(2021, 10, 8))

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders")
        }
    }

    // TODO: Consider using Buy To Open (BTO) vs. just Buy
    "create preview to buy call option limit" {
        val path = Paths.get("brokers/etrade/orders/create_preview_buy_call_option_limit.json")

        createServer(path) {

            val symbol = "SPCE"
            val year = 2021
            val month = 10
            val day = 8
            val expiry = ZonedDateTime.of(
                LocalDate.of(year, month, day),
                LocalTime.of(16, 0),
                ZoneId.of("America/New_York")
            )
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionLimit(symbol, 0.95f, 30f, 1, expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val message = data.orders.first().messages!!.messages.first()
            message.description.shouldContain("You are about to place a marketable limit order")
            message.code.shouldBe(9011)
            message.type.shouldBe(MessageType.WARNING)

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(symbol)
            product.expiry.shouldBe(GregorianCalendar(year, month, day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27825.10f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)
        }
    }

    "create preview to sell call option limit" {
        val path = Paths.get("brokers/etrade/orders/create_preview_sell_call_option_limit.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallOptionLimit(virginGalactic.symbol, 0.95f, 30f, 1, virginGalactic.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(virginGalactic.symbol)
            product.expiry.shouldBe(GregorianCalendar(virginGalactic.year, virginGalactic.month, virginGalactic.day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27825.10f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)
        }
    }

    "create preview to buy call option market" {
        val path = Paths.get("brokers/etrade/orders/create_preview_buy_call_option_market.json")

        createServer(path) {
            val strike = 150f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionMarket(apple.symbol, 1f, 0.5f, strike, 1, apple.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            data.orders.first().messages.shouldBeNull()

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(apple.symbol)
            product.expiry.shouldBe(GregorianCalendar(apple.year, apple.month, apple.day))

            val osi = "${apple.symbol}--${apple.year - 2000}${apple.month}${apple.day}C00${strike.toInt()}000"
            product.product!!.symbol.shouldBe(osi)
        }
    }

    "create preview to sell call option market" {
        val path = Paths.get("brokers/etrade/orders/create_preview_sell_call_option_market.json")

        createServer(path) {
            val strike = 150f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallOptionMarket(apple.symbol, 1f, 0.5f, strike, 1, apple.expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            data.orders.first().messages.shouldBeNull()

            val instrument = data.orders.first().instrument!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)

            val product = instrument.product!!
            product.symbol.shouldBe(apple.symbol)
            product.expiry.shouldBe(GregorianCalendar(apple.year, apple.month, apple.day))

            val osi = "${apple.symbol}--${apple.year - 2000}${apple.month}${apple.day}C00${strike.toInt()}000"
            product.product!!.symbol.shouldBe(osi)
        }
    }
})
