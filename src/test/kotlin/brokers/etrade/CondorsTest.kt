package brokers.etrade

import brokers.etrade.services.orderPreview.buyCondorCalls
import brokers.etrade.services.orderPreview.buyCondorPuts
import brokers.etrade.services.orderPreview.sellCondorCalls
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.services.Orders
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class CondorsTest : StringSpec({
    val accountIdKey = randomString(6)

    val snap = TestOption(
        "SNAP",
        2021,
        10,
        15
    )

    val quantity = 10

    "create preview to buy condor calls" {
        val path = Paths.get("brokers/etrade/orders/condors/buy_calls.json")

        createServer(path) {
            val limitPrice = 0.08f
            val strikes = listOf(78.5f, 79f, 79.5f, 80f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCondorCalls(
                "SNAP",
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(snap.symbol)
                    it.expiryYear.shouldBe(snap.year)
                    it.expiryMonth.shouldBe(snap.month)
                    it.expiryDay.shouldBe(snap.day)
                    it.callPut.shouldBe(OptionType.CALL)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy condor puts" {
        val path = Paths.get("brokers/etrade/orders/condors/buy_puts.json")

        createServer(path) {
            val limitPrice = 0.06f
            val strikes = listOf(78.5f, 79f, 79.5f, 80f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCondorPuts(
                "SNAP",
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_DEBIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(snap.symbol)
                    it.expiryYear.shouldBe(snap.year)
                    it.expiryMonth.shouldBe(snap.month)
                    it.expiryDay.shouldBe(snap.day)
                    it.callPut.shouldBe(OptionType.PUT)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell condor calls" {
        val path = Paths.get("brokers/etrade/orders/condors/sell_calls.json")

        createServer(path) {
            val limitPrice = 0.08f
            val strikes = listOf(79f, 80f, 81f, 82f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCondorCalls(
                "SNAP",
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_CREDIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(snap.symbol)
                    it.expiryYear.shouldBe(snap.year)
                    it.expiryMonth.shouldBe(snap.month)
                    it.expiryDay.shouldBe(snap.day)
                    it.callPut.shouldBe(OptionType.CALL)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell condor puts" {
        val path = Paths.get("brokers/etrade/orders/condors/sell_puts.json")

        createServer(path) {
            val limitPrice = 0.07f
            val strikes = listOf(82f, 83f, 84f, 85f)
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCondorCalls(
                "SNAP",
                Pair(strikes[0], strikes[1]),
                Pair(strikes[2], strikes[3]),
                limitPrice,
                quantity
            )
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.CONDOR)

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.NET_CREDIT)
            order.limitPrice.shouldBe(limitPrice)

            order.instruments!!.forEachIndexed { index, instrument ->
                instrument.quantity.shouldBe(quantity.toFloat())
                instrument.product!!.let {
                    it.symbol.shouldBe(snap.symbol)
                    it.expiryYear.shouldBe(snap.year)
                    it.expiryMonth.shouldBe(snap.month)
                    it.expiryDay.shouldBe(snap.day)
                    it.callPut.shouldBe(OptionType.PUT)
                    it.strikePrice.shouldBe(strikes[index])
                }

                when (index) {
                    0, 3 -> instrument.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                    1, 2 -> instrument.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                }
            }

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "invalid strikes for condor" {
        shouldThrow<IllegalArgumentException> {
            sellCondorCalls(
                "SNAP",
                Pair(79f, 80f),
                Pair(83f, 82f),
                .5f,
                quantity
            )
        }
    }
})
