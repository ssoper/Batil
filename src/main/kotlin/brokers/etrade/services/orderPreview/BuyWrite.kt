package com.seansoper.batil.brokers.etrade.services.orderPreview

import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewInstrumentEquity
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewInstrumentOption
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewOrderLimit
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewProductEquity
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewProductOption
import com.seansoper.batil.brokers.etrade.api.orderPreview.PreviewRequest
import java.time.ZonedDateTime

/**
 * Buy a buy-write
 * @param[symbol] The market symbol for the security being bought
 * @param[strike] The strike price to sell the call at
 * @param[limitPrice] The highest price at which to buy the buy-write
 * @param[quantity] The number of buy-writes to buy
 * @param[expiry] The date the sold call will expire
 * @param[clientOrderId] A reference ID generated by the developer that is used to ensure that a duplicate order is not being submitted. This reference ID may be any value of 20 or less alphanumeric characters but must be unique within the account. This field does not appear in any API responses.
 * @sample com.seansoper.batil.samples.Orders.purchaseBuyWrite
 */
fun buyBuyWrite(
    symbol: String,
    strike: Float,
    limitPrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {

    val instruments = listOf(
        PreviewInstrumentEquity(
            orderAction = OrderActionType.BUY,
            quantity = quantity * 100,
            product = PreviewProductEquity(symbol)
        ),
        PreviewInstrumentOption(
            orderAction = OrderActionType.SELL_OPEN,
            quantity = quantity,
            product = PreviewProductOption(
                symbol,
                OptionType.CALL,
                expiry,
                strike
            )
        )
    )

    return PreviewRequest(
        orderType = OrderType.BUY_WRITES,
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
