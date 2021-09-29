import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyBuyWrite
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class BuyWriteTest : StringSpec({

    val accountIdKey = randomString(6)

    val ticker = TestOption(
        "PLTR",
        2021,
        10,
        15
    )

    val quantity = 1

    "create preview to buy a buy-write" {
        val path = Paths.get("brokers/etrade/orders/createPreview/buyWrites/buy.json")

        createServer(path) {
            val limitPrice = 24.72f
            val strike = 26f
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyBuyWrite(
                ticker.symbol,
                strike,
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.BUY_WRITES)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!![0].let {
                it.orderAction.shouldBe(OrderActionType.BUY)
                it.quantity.shouldBe(quantity * 100.0f)
                it.product!!.symbol.shouldBe(ticker.symbol)
                it.product!!.securityType.shouldBe(SecurityType.EQ)
            }

            order.instruments!![1].let {
                it.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                it.quantity.shouldBe(quantity.toFloat())
                it.product!!.let { product ->
                    product.symbol.shouldBe(ticker.symbol)
                    product.expiryYear.shouldBe(ticker.year)
                    product.expiryMonth.shouldBe(ticker.month)
                    product.expiryDay.shouldBe(ticker.day)
                    product.callPut.shouldBe(OptionType.CALL)
                    product.strikePrice.shouldBe(strike)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
