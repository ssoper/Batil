package brokers.etrade.services.orderPreview

import brokers.etrade.api.orderPreview.PreviewInstrumentOption
import brokers.etrade.api.orderPreview.PreviewOrderLimit
import brokers.etrade.api.orderPreview.PreviewOrderMarket
import brokers.etrade.api.orderPreview.PreviewProductOption
import brokers.etrade.api.orderPreview.PreviewRequest
import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderType
import java.time.ZonedDateTime

fun buyCallOptionLimit(
    symbol: String,
    limitPrice: Float,
    strikePrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {

    return PreviewRequest(
        orderType = OrderType.OPTN,
        clientOrderId = clientOrderId,
        orders = listOf(
            PreviewOrderLimit(
                limitPrice = limitPrice,
                instruments = listOf(
                    PreviewInstrumentOption(
                        orderAction = OrderActionType.BUY_OPEN,
                        quantity = quantity,
                        product = PreviewProductOption(
                            symbol,
                            OptionType.CALL,
                            expiry,
                            strikePrice
                        )
                    )
                )
            )
        )
    )
}

fun buyCallOptionMarket(
    symbol: String,
    limitPrice: Float,
    stopPrice: Float,
    strikePrice: Float,
    quantity: Int,
    expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
    clientOrderId: String = randomString()
): PreviewRequest {

    return PreviewRequest(
        orderType = OrderType.OPTN,
        clientOrderId = clientOrderId,
        orders = listOf(
            PreviewOrderMarket(
                limitPrice = limitPrice,
                stopPrice = stopPrice,
                instruments = listOf(
                    PreviewInstrumentOption(
                        orderAction = OrderActionType.BUY_OPEN,
                        quantity = quantity,
                        product = PreviewProductOption(
                            symbol,
                            OptionType.CALL,
                            expiry,
                            strikePrice
                        )
                    )
                )
            )
        )
    )
}

private fun randomString(length: Int = 20): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}
