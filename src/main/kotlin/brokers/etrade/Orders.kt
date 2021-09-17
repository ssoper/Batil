package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
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
}
