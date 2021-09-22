package com.seansoper.batil.brokers.etrade.services

import brokers.etrade.api.orderPreview.PreviewRequest
import com.fasterxml.jackson.databind.module.SimpleModule
import com.seansoper.batil.brokers.etrade.api.OrdersApi
import com.seansoper.batil.brokers.etrade.api.OrdersResponse
import com.seansoper.batil.brokers.etrade.api.PreviewOrderResponse
import com.seansoper.batil.brokers.etrade.api.PreviewRequestEnvelope
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.deserializers.TimestampDeserializer
import java.time.Instant
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

    fun createPreview(
        accountIdKey: String,
        request: PreviewRequest
    ): PreviewOrderResponse? {
        val service = createClient(OrdersApi::class.java)
        val response = service.createPreview(accountIdKey, PreviewRequestEnvelope(request)).execute()

        return response.body()?.response
    }
}
