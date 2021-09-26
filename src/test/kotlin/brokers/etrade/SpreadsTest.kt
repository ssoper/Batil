import brokers.etrade.services.orderPreview.buyCallSpread
import brokers.etrade.services.orderPreview.buyPutSpread
import brokers.etrade.services.orderPreview.sellCallSpread
import com.seansoper.batil.brokers.etrade.api.MarginLevel
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.Orders
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import testHelper.MockHelper.createServer
import testHelper.MockHelper.mockSession
import testHelper.PathHelper.randomString
import testHelper.TestOption
import java.nio.file.Paths

class SpreadsTest : StringSpec({
    val accountIdKey = randomString(6)

    val cliffs = TestOption(
        "CLF",
        2021,
        10,
        15
    )

    "create preview to buy call debit spread" {
        val path = Paths.get("brokers/etrade/orders/spreads/create_preview_buy_call_debit.json")

        createServer(path) {
            val buyStrike = 21f
            val sellStrike = 22f
            val limitPrice = 0.32f
            val quantity = 1
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyCallSpread(cliffs.symbol, limitPrice, buyStrike, sellStrike, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.SPREADS)
            data.totalOrderValue.shouldBe(33.0264f)

            data.orders.first().let {
                it.messages.shouldBeNull()
                it.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
                it.priceType.shouldBe(OrderPriceType.NET_DEBIT)
                it.limitPrice.shouldBe(limitPrice)
            }

            data.orders.first().instruments!!.first().let {
                it.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(buyStrike)
                    it.expiryYear.shouldBe(cliffs.year)
                    it.expiryMonth.shouldBe(cliffs.month)
                    it.expiryDay.shouldBe(cliffs.day)
                }
            }

            data.orders.first().instruments!![1].let {
                it.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(sellStrike)
                }
            }

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(34595.17f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to sell call credit spread" {
        val path = Paths.get("brokers/etrade/orders/spreads/create_preview_sell_call_credit.json")

        createServer(path) {
            val buyStrike = 23f
            val sellStrike = 22f
            val limitPrice = 0.20f
            val quantity = 1
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = sellCallSpread(cliffs.symbol, limitPrice, buyStrike, sellStrike, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.SPREADS)
            data.totalOrderValue.shouldBe(-18.9736f)

            data.orders.first().let {
                it.messages.shouldBeNull()
                it.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
                it.priceType.shouldBe(OrderPriceType.NET_CREDIT)
                it.limitPrice.shouldBe(limitPrice)
            }

            data.orders.first().instruments!!.first().let {
                it.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(buyStrike)
                    it.expiryYear.shouldBe(cliffs.year)
                    it.expiryMonth.shouldBe(cliffs.month)
                    it.expiryDay.shouldBe(cliffs.day)
                }
            }

            data.orders.first().instruments!![1].let {
                it.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(sellStrike)
                }
            }

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(34595.17f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }

    "create preview to buy put debit spread" {
        val path = Paths.get("brokers/etrade/orders/spreads/create_preview_buy_put_debit.json")

        createServer(path) {
            val buyStrike = 19f
            val sellStrike = 18f
            val limitPrice = 0.22f
            val quantity = 1
            val service = Orders(mockSession(), baseUrl = it.url(".").toString())
            val request = buyPutSpread(cliffs.symbol, limitPrice, buyStrike, sellStrike, quantity)
            val data = service.createPreview(accountIdKey, request)

            data.shouldNotBeNull()
            data.orderType.shouldBe(OrderType.SPREADS)
            data.totalOrderValue.shouldBe(23.0264f)

            data.orders.first().let {
                it.messages.shouldBeNull()
                it.orderTerm.shouldBe(OrderTerm.GOOD_FOR_DAY)
                it.priceType.shouldBe(OrderPriceType.NET_DEBIT)
                it.limitPrice.shouldBe(limitPrice)
            }

            data.orders.first().instruments!!.first().let {
                it.orderAction.shouldBe(OrderActionType.BUY_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(buyStrike)
                    it.expiryYear.shouldBe(cliffs.year)
                    it.expiryMonth.shouldBe(cliffs.month)
                    it.expiryDay.shouldBe(cliffs.day)
                }
            }

            data.orders.first().instruments!![1].let {
                it.orderAction.shouldBe(OrderActionType.SELL_OPEN)
                it.quantity!!.toInt().shouldBe(quantity)
                it.product!!.let {
                    it.symbol.shouldBe(cliffs.symbol)
                    it.securityType.shouldBe(SecurityType.OPTN)
                    it.strikePrice.shouldBe(sellStrike)
                }
            }

            data.marginLevel.shouldBe(MarginLevel.MARGIN_TRADING_ALLOWED)
            data.margin!!.marginable!!.currentBuyingPower.shouldBe(34595.17f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/orders/preview")
        }
    }
})
