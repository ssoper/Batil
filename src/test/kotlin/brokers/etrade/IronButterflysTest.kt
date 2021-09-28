import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyButterflyCalls
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class IronButterflysTest : StringSpec({

    val accountIdKey = randomString(6)

    val ticker = TestOption(
        "TSP",
        2021,
        10,
        15
    )

    val quantity = 10

    "create preview to buy an iron butterfly" {
        val path = Paths.get("brokers/etrade/orders/createPreview/ironButterflys/buy.json")

        createServer(path) {
            val limitPrice = 0.20f
            val strikes = listOf(30f, 35f, 40f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyButterflyCalls(
                ticker.symbol,
                Triple(strikes[0], strikes[1], strikes[2]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.IRON_BUTTERFLY)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(ticker.symbol)
                    it.expiryYear.shouldBe(ticker.year)
                    it.expiryMonth.shouldBe(ticker.month)
                    it.expiryDay.shouldBe(ticker.day)

                    if (index > 1) {
                        it.strikePrice.shouldBe(strikes[index - 1])
                    } else {
                        it.strikePrice.shouldBe(strikes[index])
                    }

                    when (index) {
                        0, 1 -> it.callPut.shouldBe(OptionType.PUT)
                        else -> it.callPut.shouldBe(OptionType.CALL)
                    }
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                    else -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
