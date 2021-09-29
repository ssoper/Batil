import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyButterflyCalls
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyButterflyPuts
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellButterflyCalls
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellButterflyPuts
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class ButterflysTest : StringSpec({

    val accountIdKey = randomString(6)

    val chargePoint = TestOption(
        "CHPT",
        2021,
        10,
        15
    )

    val quantity = 10

    "create preview to buy butterfly calls" {
        val path = Paths.get("brokers/etrade/orders/createPreview/butterflys/buy_calls.json")

        createServer(path) {
            val limitPrice = 0.19f
            val strikes = listOf(18f, 19f, 20f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyButterflyCalls(
                chargePoint.symbol,
                Triple(strikes[0], strikes[1], strikes[2]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.BUTTERFLY)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.product!!.let {
                    it.symbol.shouldBe(chargePoint.symbol)
                    it.expiryYear.shouldBe(chargePoint.year)
                    it.expiryMonth.shouldBe(chargePoint.month)
                    it.expiryDay.shouldBe(chargePoint.day)
                    it.callPut.shouldBe(OptionType.CALL)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 2 -> {
                        instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat())
                    }
                    else -> {
                        instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat() * 2)
                    }
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell butterfly calls" {
        val path = Paths.get("brokers/etrade/orders/createPreview/butterflys/sell_calls.json")

        createServer(path) {
            val limitPrice = 0.19f
            val strikes = listOf(18f, 19f, 20f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellButterflyCalls(
                chargePoint.symbol,
                Triple(strikes[0], strikes[1], strikes[2]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.BUTTERFLY)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_CREDIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.product!!.let {
                    it.symbol.shouldBe(chargePoint.symbol)
                    it.expiryYear.shouldBe(chargePoint.year)
                    it.expiryMonth.shouldBe(chargePoint.month)
                    it.expiryDay.shouldBe(chargePoint.day)
                    it.callPut.shouldBe(OptionType.CALL)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 2 -> {
                        instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat())
                    }
                    else -> {
                        instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat() * 2)
                    }
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy butterfly puts" {
        val path = Paths.get("brokers/etrade/orders/createPreview/butterflys/buy_puts.json")

        createServer(path) {
            val limitPrice = 0.19f
            val strikes = listOf(18f, 19f, 20f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyButterflyPuts(
                chargePoint.symbol,
                Triple(strikes[0], strikes[1], strikes[2]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.BUTTERFLY)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.product!!.let {
                    it.symbol.shouldBe(chargePoint.symbol)
                    it.expiryYear.shouldBe(chargePoint.year)
                    it.expiryMonth.shouldBe(chargePoint.month)
                    it.expiryDay.shouldBe(chargePoint.day)
                    it.callPut.shouldBe(OptionType.PUT)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 2 -> {
                        instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat())
                    }
                    else -> {
                        instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat() * 2)
                    }
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell butterfly puts" {
        val path = Paths.get("brokers/etrade/orders/createPreview/butterflys/sell_puts.json")

        createServer(path) {
            val limitPrice = 0.19f
            val strikes = listOf(18f, 19f, 20f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellButterflyPuts(
                chargePoint.symbol,
                Triple(strikes[0], strikes[1], strikes[2]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.BUTTERFLY)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_CREDIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.product!!.let {
                    it.symbol.shouldBe(chargePoint.symbol)
                    it.expiryYear.shouldBe(chargePoint.year)
                    it.expiryMonth.shouldBe(chargePoint.month)
                    it.expiryDay.shouldBe(chargePoint.day)
                    it.callPut.shouldBe(OptionType.PUT)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 2 -> {
                        instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat())
                    }
                    else -> {
                        instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                        instrument.quantity.shouldBe(quantity.toFloat() * 2)
                    }
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
