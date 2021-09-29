import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyIronCondor
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellIronCondor
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class IronCondorsTest : StringSpec({
    val accountIdKey = randomString(6)

    val aurora = TestOption(
        "ACB",
        2021,
        10,
        15
    )

    val limitPrice = 0.36f
    val quantity = 10

    "create preview to buy iron condors" {
        val path = Paths.get("brokers/etrade/orders/createPreview/ironCondors/buy.json")

        createServer(path) {
            val strikes = listOf(5.5f, 6f, 6.5f, 7f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyIronCondor(
                aurora.symbol,
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.IRON_CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(aurora.symbol)
                    it.expiryYear.shouldBe(aurora.year)
                    it.expiryMonth.shouldBe(aurora.month)
                    it.expiryDay.shouldBe(aurora.day)
                    it.strikePrice.shouldBe(strikes[index])

                    when (index) {
                        0, 1 -> it.callPut.shouldBe(OptionType.PUT)
                        2, 3 -> it.callPut.shouldBe(OptionType.CALL)
                    }
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell condor calls" {
        val path = Paths.get("brokers/etrade/orders/createPreview/ironCondors/sell.json")

        createServer(path) {
            val strikes = listOf(5.5f, 6f, 6.5f, 7f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellIronCondor(
                aurora.symbol,
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.IRON_CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_CREDIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(aurora.symbol)
                    it.expiryYear.shouldBe(aurora.year)
                    it.expiryMonth.shouldBe(aurora.month)
                    it.expiryDay.shouldBe(aurora.day)
                    it.strikePrice.shouldBe(strikes[index])

                    when (index) {
                        0, 1 -> it.callPut.shouldBe(OptionType.PUT)
                        2, 3 -> it.callPut.shouldBe(OptionType.CALL)
                    }
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
