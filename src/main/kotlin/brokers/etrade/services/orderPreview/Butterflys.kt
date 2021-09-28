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
internal fun validateStrikes(
    strikes: Triple<Float, Float, Float>
) {
    require(strikes.first < strikes.second) { "First strike in wing must be less than the second strike" }
    require(strikes.second < strikes.third) { "Second strike in lower wing must be less than the third strike" }
}

/**
 * @suppress
 */
internal fun createButterflyInstruments(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    quantity: Int,
    expiry: ZonedDateTime,
    optionType: OptionType,
    long: Boolean,
): List<PreviewInstrumentOption> {
    validateStrikes(strikes)

    return strikes.toList().mapIndexed { index, strike ->
        val action = when (index) {
            0, 2 -> if (long) { OrderActionType.BUY_OPEN } else { OrderActionType.SELL_OPEN }
            else -> if (long) { OrderActionType.SELL_OPEN } else { OrderActionType.BUY_OPEN }
        }

        val strikeQuantity = when (index) {
            0, 2 -> quantity
            else -> quantity * 2
        }

        PreviewInstrumentOption(
            orderAction = action,
            quantity = strikeQuantity,
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
 * Buy a butterfly with calls
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the butterfly
 * @param[limitPrice] The highest price at which to buy the butterfly
 * @param[quantity] The number of butterflies to buy
 * @param[expiry] The date the butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 * @sample com.seansoper.batil.samples.Orders.buyButterflyCalls
 */
fun buyButterflyCalls(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createButterflyInstruments(symbol, strikes, quantity, expiry, OptionType.CALL, true)

    return PreviewRequest(
        orderType = OrderType.BUTTERFLY,
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
 * Sell a butterfly with calls
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the butterfly
 * @param[limitPrice] The highest price at which to sell the butterfly
 * @param[quantity] The number of butterflies to sell
 * @param[expiry] The date the butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 * @sample com.seansoper.batil.samples.Orders.buyButterflyCalls
 */
fun sellButterflyCalls(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createButterflyInstruments(symbol, strikes, quantity, expiry, OptionType.CALL, false)

    return PreviewRequest(
        orderType = OrderType.BUTTERFLY,
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

/**
 * Buy a butterfly with puts
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the butterfly
 * @param[limitPrice] The highest price at which to buy the butterfly
 * @param[quantity] The number of butterflies to buy
 * @param[expiry] The date the butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 */
fun buyButterflyPuts(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createButterflyInstruments(symbol, strikes, quantity, expiry, OptionType.PUT, true)

    return PreviewRequest(
        orderType = OrderType.BUTTERFLY,
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
 * Sell a butterfly with puts
 * @param[symbol] The market symbol for the security being bought
 * @param[strikes] A triple representing the strike prices for the butterfly
 * @param[limitPrice] The highest price at which to sell the butterfly
 * @param[quantity] The number of butterflies to sell
 * @param[expiry] The date the butterflies will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 * @sample com.seansoper.batil.samples.Orders.buyButterflyCalls
 */
fun sellButterflyPuts(
    symbol: String,
    strikes: Triple<Float, Float, Float>,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {
    val instruments = createButterflyInstruments(symbol, strikes, quantity, expiry, OptionType.PUT, false)

    return PreviewRequest(
        orderType = OrderType.BUTTERFLY,
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