import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.MarketSession
import com.seansoper.batil.brokers.etrade.services.OrderStatus
import com.seansoper.batil.brokers.etrade.services.OrderTransactionType
import com.seansoper.batil.brokers.etrade.services.Orders
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

    "list orders with query" {
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

            it.takeRequest().path.let {
                it.shouldContain("count=10")
                it.shouldContain("status=EXECUTED")
                it.shouldContain("fromDate=08012021")
                it.shouldContain("toDate=1011202")
                it.shouldContain("symbol=RIOT")
                it.shouldContain("securityType=OPTN")
                it.shouldContain("transactionType=SELL_SHORT")
                it.shouldContain("marketSession=REGULAR")
                it.shouldContain("/v1/accounts/$accountIdKey/orders")
            }
        }
    }
})
