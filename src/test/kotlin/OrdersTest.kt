import TestHelper.MockHelper.createServer
import TestHelper.MockHelper.mockSession
import TestHelper.PathHelper.randomString
import com.seansoper.batil.brokers.etrade.OptionType
import com.seansoper.batil.brokers.etrade.OrderActionType
import com.seansoper.batil.brokers.etrade.OrderStatus
import com.seansoper.batil.brokers.etrade.OrderTerm
import com.seansoper.batil.brokers.etrade.OrderType
import com.seansoper.batil.brokers.etrade.Orders
import com.seansoper.batil.brokers.etrade.SecurityType
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths
import java.time.Instant
import java.util.GregorianCalendar

class OrdersTest : StringSpec({

    "list orders" {
        val path = Paths.get("apiResponses/orders/list.json")

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
})
