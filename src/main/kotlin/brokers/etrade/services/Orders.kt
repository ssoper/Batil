package com.seansoper.batil.brokers.etrade.services

import com.fasterxml.jackson.databind.module.SimpleModule
import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.api.CreatePreviewEnvelope
import com.seansoper.batil.brokers.etrade.api.CreatePreviewRequest
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.OrdersApi
import com.seansoper.batil.brokers.etrade.api.OrdersResponse
import com.seansoper.batil.brokers.etrade.api.PreviewInstrumentOption
import com.seansoper.batil.brokers.etrade.api.PreviewOrderLimit
import com.seansoper.batil.brokers.etrade.api.PreviewOrderResponse
import com.seansoper.batil.brokers.etrade.api.PreviewProductOption
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.deserializers.TimestampDeserializer
import java.time.Instant
import java.time.ZonedDateTime
import java.util.GregorianCalendar

enum class OrderStatus {
    OPEN, EXECUTED, CANCELLED, INDIVIDUAL_FILLS, CANCEL_REQUESTED, EXPIRED, REJECTED, PARTIAL, DO_NOT_EXERCISE, DONE_TRADE_EXECUTED
}

enum class OrderTransactionType {
    ATNM, BUY, SELL, SELL_SHORT, BUY_TO_COVER, MF_EXCHANGE
}

class Orders(
    session: Session,
    production: Boolean? = null,
    verbose: Boolean? = null,
    baseUrl: String? = null
) : Service(session, production, verbose, baseUrl) {

    /**
     * List orders for an account
     * @param[accountIdKey] The unique account key
     * @param[marker] Specifies the desired starting point of the set of items to return, used for paging
     * @param[count] Number of transactions to return in the response, defaults to 25, max is 100, used for paging
     * @param[status] Filter by status
     * @param[fromDate] The earliest date to include in the date range, history is available for two years
     * @param[toDate] The latest date to include in the date range, both fromDate and toDate should be provided and
     * toDate should be greater than fromDate
     * @param[symbol] List of market symbols for the securities being bought or sold, length should be less than or
     * equal to 25
     * @param[securityType] The security type
     * @param[transactionType] Type of transaction
     * @param[marketSession] Session in which the equity order will have been placed
     * @sample com.seansoper.batil.samples.Orders.list
     */
    fun list(
        accountIdKey: String,
        marker: String? = null,
        count: Int? = null,
        status: OrderStatus? = null,
        fromDate: GregorianCalendar? = null,
        toDate: GregorianCalendar? = null,
        symbol: List<String>? = null,
        securityType: SecurityType? = null,
        transactionType: OrderTransactionType? = null,
        marketSession: MarketSession? = null
    ): OrdersResponse? {
        val options: MutableMap<String, String> = mutableMapOf()

        marker?.let {
            options.put("marker", it)
        }

        count?.let {
            options.put("count", count.toString())
        }

        status?.let {
            options.put("status", it.name)
        }

        fromDate?.let {
            options.put("fromDate", formatDate(it))
        }

        toDate?.let {
            options.put("toDate", formatDate(it))
        }

        symbol?.let {
            options.put("symbol", it.joinToString(","))
        }

        securityType?.let {
            options.put("securityType", it.name)
        }

        transactionType?.let {
            options.put("transactionType", it.name)
        }

        marketSession?.let {
            options.put("marketSession", it.name)
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(OrdersApi::class.java, module)
        val response = service.list(accountIdKey, options).execute()

        return response.body()?.response
    }

    // try to create just the bare minimum option
    // send typed json data
    // send xml
    // TODO: Create order
    // TODO: Preview order
    // TODO: Place order
    // TODO: Equities, options, Spreads to start
    // TODO: Investigate use of original Order API classes + Builder pattern vs. specific classes for creating Preview
    // TODO: Or possibly better use of Jackson annotations https://www.baeldung.com/jackson-annotations
    // TODO: Or using constructors with the general nullable classes but then we lose our int conversion
    fun createPreview(
        accountIdKey: String,
        symbol: String,
        limitPrice: Float,
        strikePrice: Float,
        quantity: Int,
        expiry: ZonedDateTime = OptionsCalendar.nextMonthly(),
        clientOrderId: String = randomString(20)
    ): PreviewOrderResponse? {

        val body = CreatePreviewRequest(
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

        val request = CreatePreviewEnvelope(
            request = body
        )

        val service = createClient(OrdersApi::class.java)
        val response = service.createPreview(accountIdKey, request).execute()

        return response.body()?.response
    }

    private fun randomString(length: Int = 15): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}
