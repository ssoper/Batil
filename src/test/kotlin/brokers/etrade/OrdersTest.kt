import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.MarketSession
import com.seansoper.batil.brokers.etrade.services.OrderStatus
import com.seansoper.batil.brokers.etrade.services.OrderTransactionType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyEquityLimit
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import java.nio.file.Paths
import java.time.Instant
import java.util.GregorianCalendar

class OrdersTest : StringSpec({
    val accountIdKey = randomString(6)

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

            val instrument = details.instruments!!.first()
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

    "list orders with query params" {
        val path = Paths.get("brokers/etrade/orders/list_query.json")

        createServer(path) {
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val data = service.list(
                accountIdKey,
                count = 10,
                status = OrderStatus.EXECUTED,
                fromDate = GregorianCalendar(2021, 7, 1),
                toDate = GregorianCalendar(2021, 9, 11),
                symbol = "RIOT",
                securityType = SecurityType.OPTN,
                transactionType = OrderTransactionType.SELL_SHORT,
                marketSession = MarketSession.REGULAR
            )

            data.shouldNotBeNull()
            data.orders!!.size.shouldBe(2)

            it.takeRequest().path.let { url ->
                url.shouldContain("count=10")
                url.shouldContain("status=EXECUTED")
                url.shouldContain("fromDate=08012021")
                url.shouldContain("toDate=1011202")
                url.shouldContain("symbol=RIOT")
                url.shouldContain("securityType=OPTN")
                url.shouldContain("transactionType=SELL_SHORT")
                url.shouldContain("marketSession=REGULAR")
                url.shouldContain("/v1/accounts/$accountIdKey/orders")
            }
        }
    }

    "place order" {
        val previewOrderPath = Paths.get("brokers/etrade/orders/placeOrder/preview_order.json")

        createServer(previewOrderPath) {
            val equity = Triple("PLTR", 21f, 1)
            val previewService = Orders(mockSession(), baseUrl = it.url(".").toString())
            val previewRequest = buyEquityLimit(equity.first, equity.second, equity.third)
            val previewResponse = previewService.createPreview(accountIdKey, previewRequest)
            val placeOrderPath = Paths.get("brokers/etrade/orders/placeOrder/place_order.json")

            createServer(placeOrderPath) {
                val service = Orders(mockSession(), baseUrl = it.url(".").toString())
                val data = service.placeOrder(accountIdKey, previewRequest, previewResponse!!)

                data.shouldNotBeNull()
                data.orderType.shouldBe(OrderType.EQ)
                data.orderId.shouldBe(402)

                val order = data.orders.first()
                order.limitPrice.shouldBe(equity.second)

                val instrument = order.instruments!!.first()
                instrument.quantity.shouldBe(equity.third.toFloat())
                instrument.product!!.symbol.shouldBe(equity.first)

                it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/place")
            }
        }
    }

    "cancel order" {
        val path = Paths.get("brokers/etrade/orders/cancel_order.json")

        createServer(path) {
            val orderId = 403L
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val data = service.cancelOrder(accountIdKey, orderId)

            data.shouldNotBeNull()

            data.orderId.shouldBe(orderId)
            data.messages!!.messages.first().code.shouldBe(5011)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/cancel")
        }
    }
})
