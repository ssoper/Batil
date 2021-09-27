import com.seansoper.batil.brokers.etrade.api.MarginLevel
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyEquityLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyEquityMarket
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellEquityLimit
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellEquityMarket
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class EquitiesTest : StringSpec({
    val accountIdKey = randomString(6)
    val symbol = "RIOT"
    val limitPrice = 27f

    "create preview to buy equities limit" {
        val path = Paths.get("brokers/etrade/orders/equities/create_preview_buy_limit.json")

        createServer(path) {
            val quantity = 50
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyEquityLimit(symbol, limitPrice, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.LIMIT)
            order.limitPrice.shouldBe(limitPrice)

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY)
            instrument.quantity!!.toInt().shouldBe(quantity)

            val product = instrument.product!!
            product.symbol.shouldBe(symbol)
            product.securityType.shouldBe(SecurityType.EQ)

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27824.2f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell equities limit" {
        val path = Paths.get("brokers/etrade/orders/equities/create_preview_sell_limit.json")

        createServer(path) {
            val quantity = 200
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellEquityLimit(symbol, limitPrice, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(34595.17f)

            val order = data.orders.first()
            order.priceType.shouldBe(OrderPriceType.LIMIT)
            order.limitPrice.shouldBe(limitPrice)
            order.estimatedTotalAmount.shouldBe(-5399.9486f)

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL)
            instrument.quantity!!.toInt().shouldBe(quantity)

            val product = instrument.product!!
            product.symbol.shouldBe(symbol)
            product.securityType.shouldBe(SecurityType.EQ)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy equities market" {
        val path = Paths.get("brokers/etrade/orders/equities/create_preview_buy_market.json")

        createServer(path) {
            val stopPrice = 0f
            val quantity = 50
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyEquityMarket(symbol, limitPrice, stopPrice, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()

            val order = data.orders.first()
            order.messages.shouldBeNull()
            order.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
            order.priceType.shouldBe(OrderPriceType.MARKET)
            order.limitPrice.shouldBe(0f)
            order.estimatedTotalAmount.shouldBe(1390f)

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.BUY)
            instrument.quantity!!.toInt().shouldBe(quantity)

            val product = instrument.product!!
            product.symbol.shouldBe(symbol)
            product.securityType.shouldBe(SecurityType.EQ)

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(27824.2f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell equities market" {
        val path = Paths.get("brokers/etrade/orders/equities/create_preview_sell_market.json")

        createServer(path) {
            val quantity = 200
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellEquityMarket(symbol, limitPrice, 0f, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(34098.89f)

            val order = data.orders.first()
            order.priceType.shouldBe(OrderPriceType.MARKET)
            order.limitPrice.shouldBe(0f)
            order.estimatedTotalAmount.shouldBe(-5509.948f)

            val instrument = data.orders.first().instruments!!.first()
            instrument.orderAction.shouldBe(OrderActionType.SELL)
            instrument.quantity!!.toInt().shouldBe(quantity)

            val product = instrument.product!!
            product.symbol.shouldBe(symbol)
            product.securityType.shouldBe(SecurityType.EQ)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

})
