package com.seansoper.batil.brokers.etrade.services.orderPreview

import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewInstrumentOption
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewOrderLimit
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewProductOption
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewRequest
import java.time.ZonedDateTime

/**
 * @suppress
 */
internal fun createIronButterflyInstruments(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    quantity: Int,
    expiry: ZonedDateTime,
    long: Boolean,
): List<PreviewInstrumentOption> {
    validateStrikes(strikes)

    val flyStrikes = listOf(strikes.first, strikes.second, strikes.second, strikes.third)

    return flyStrikes.mapIndexed { index, strike ->
        val action = when (index) {
            0, 3 -> if (long) { OrderActionType.SELL_OPEN } else { OrderActionType.BUY_OPEN }
            else -> if (long) { OrderActionType.BUY_OPEN } else { OrderActionType.SELL_OPEN }
        }

        val optionType = when (index) {
            0, 1 -> OptionType.PUT
            else -> OptionType.CALL
        }

        PreviewInstrumentOption(
            orderAction = action,
            quantity = quantity,
            product = PreviewProductOption(
                symbol,
                optionType,
                expiry,
                strike
            )
        )
    }
}

/**
 * Buy an iron butterfly
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the iron butterfly
 * @param[limitPrice] The highest price at which to buy the iron butterfly
 * @param[quantity] The number of iron butterflies to buy
 * @param[expiry] The date the iron butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 */
fun buyIronButterfly(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createIronButterflyInstruments(symbol, strikes, quantity, expiry, true)

    return PreviewRequest(
        orderType = OrderType.IRON_BUTTERFLY,
        clientOrderId = clientOrderId,
        orders = listOf(
            PreviewOrderLimit(
                limitPrice = limitPrice,
                priceType = OrderPriceType.NET_DEBIT,
                instruments = instruments
            )
        )
    )
}

/**
 * Sell an iron butterfly
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the iron butterfly
 * @param[limitPrice] The highest price at which to sell the iron butterfly
 * @param[quantity] The number of iron butterflies to sell
 * @param[expiry] The date the iron butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 */
fun sellIronButterfly(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createIronButterflyInstruments(symbol, strikes, quantity, expiry, false)

    return PreviewRequest(
        orderType = OrderType.IRON_BUTTERFLY,
        clientOrderId = clientOrderId,
        orders = listOf(
            PreviewOrderLimit(
                limitPrice = limitPrice,
                priceType = OrderPriceType.NET_CREDIT,
                instruments = instruments
            )
        )
    )
}
