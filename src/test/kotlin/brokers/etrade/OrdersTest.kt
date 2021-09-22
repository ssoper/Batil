import brokers.etrade.services.orderPreview.buyCallOptionLimit
import brokers.etrade.services.orderPreview.buyCallOptionMarket
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
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

class OrdersTest : StringSpec({

    "list orders" {
        val path = Paths.get("brokers/etrade/orders/list.json")

        createServer(path) {
            val accountIdKey = randomString(6)
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

    "create preview to buy call option limit" {
        val path = Paths.get("brokers/etrade/orders/create_preview_buy_call_option_limit.json")

        createServer(path) {
            val accountIdKey = randomString(6)
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

            val product = data.orders.first().instrument!!.first().product!!
            product.symbol.shouldBe(symbol)
            product.expiry.shouldBe(GregorianCalendar(year, month, day))

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27825.10f)
            data.optionLevel.shouldBe(OptionLevel.LEVEL_3)
        }
    }

    "create preview to buy call option market" {
        val path = Paths.get("brokers/etrade/orders/create_preview_buy_call_option_market.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val symbol = "AAPL"
            val strike = 150f
            val year = 2021
            val month = 10
            val day = 15
            val expiry = ZonedDateTime.of(
                LocalDate.of(year, month, day),
                LocalTime.of(16, 0),
                ZoneId.of("America/New_York")
            )

            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallOptionMarket(symbol, 1f, 0.5f, strike, 1, expiry)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            data.orders.first().messages.shouldBeNull()

            val product = data.orders.first().instrument!!.first().product!!
            product.symbol.shouldBe(symbol)
            product.expiry.shouldBe(GregorianCalendar(year, month, day))

            val osi = "$symbol--${year - 2000}$month${day}C00${strike.toInt()}000"
            product.product!!.symbol.shouldBe(osi)
        }
    }
})
