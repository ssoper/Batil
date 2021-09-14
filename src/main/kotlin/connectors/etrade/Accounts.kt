package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.GregorianCalendar

enum class TransactionSortOrder {
    ASC,
    DESC // Default
}

enum class PortfolioSortBy {
    SYMBOL, TYPE_NAME, EXCHANGE_NAME, CURRENCY, QUANTITY, LONG_OR_SHORT, DATE_ACQUIRED, PRICEPAID, TOTAL_GAIN, TOTAL_GAIN_PCT, MARKET_VALUE, BI, ASK, PRICE_CHANGE, PRICE_CHANGE_PCT, VOLUME, WEEK_52_HIGH, WEEK_52_LOW, EPS, PE_RATIO, OPTION_TYPE, STRIKE_PRICE, PREMIUM, EXPIRATION, DAYS_GAIN, COMMISSION, MARKETCAP, PREV_CLOSE, OPEN, DAYS_RANGE, TOTAL_COST, DAYS_GAIN_PCT, PCT_OF_PORTFOLIO, LAST_TRADE_TIME, BASE_SYMBOL_PRICE, WEEK_52_RANGE, LAST_TRADE, SYMBOL_DESC, BID_SIZE, ASK_SIZE, OTHER_FEES, HELD_AS, OPTION_MULTIPLIER, DELIVERABLES, COST_PERSHARE, DIVIDEND, DIV_YIELD, DIV_PAY_DATE, EST_EARN, EX_DIV_DATE, TEN_DAY_AVG_VOL, BETA, BID_ASK_SPREAD, MARGINABLE, DELTA_52WK_HI, DELTA_52WK_LOW, PERF_1MON, ANNUAL_DIV, PERF_12MON, PERF_3MON, PERF_6MON, PRE_DAY_VOL, SV_1MON_AVG, SV_10DAY_AVG, SV_20DAY_AVG, SV_2MON_AVG, SV_3MON_AVG, SV_4MON_AVG, SV_6MON_AVG, DELTA, GAMMA, IV_PCT, THETA, VEGA, ADJ_NONADJ_FLAG, DAYS_EXPIRATION, OPEN_INTEREST, INSTRINIC_VALUE, RHO, TYPE_CODE, DISPLAY_SYMBOL, AFTER_HOURS_PCTCHANGE, PRE_MARKET_PCTCHANGE, EXPAND_COLLAPSE_FLAG
}

enum class MarketSession {
    REGULAR, // Default
    EXTENDED
}

enum class PortfolioView {
    PERFORMANCE, FUNDAMENTAL, OPTIONSWATCH, QUICK, COMPLETE
}

class Accounts(
    session: Session,
    production: Boolean? = null,
    verbose: Boolean? = null,
    baseUrl: String? = null
) : Service(session, production, verbose, baseUrl) {

    fun list(): List<Account>? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getAccounts().execute()

        return response.body()?.response?.accountRoot?.accounts
    }

    fun getBalance(accountIdKey: String): AccountBalance? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getBalance(accountIdKey).execute()

        return response.body()?.response
    }

    fun listTransactions(accountIdKey: String): TransactionResponse? {
        return listTransactions(accountIdKey, startDate = null, endDate = null, sortOrder = null, startAt = null)
    }

    fun listTransactions(
        accountIdKey: String,
        startDate: GregorianCalendar,
        endDate: GregorianCalendar
    ): TransactionResponse? {
        return listTransactions(accountIdKey, startDate = startDate, endDate = endDate, sortOrder = null, startAt = null)
    }

    // TODO: Implement marker

    fun formatDate(date: GregorianCalendar): String {
        val formatter = SimpleDateFormat("MMddyyyy")
        formatter.calendar = date
        return formatter.format(date.time)
    }

    // TODO: Implement startAt
    // TODO: Move to default null for count and other fields
    fun listTransactions(
        accountIdKey: String,
        startDate: GregorianCalendar?,
        endDate: GregorianCalendar?,
        sortOrder: TransactionSortOrder?,
        startAt: TransactionId?,
        count: Int? = 50
    ): TransactionResponse? {

        val options = mutableMapOf("count" to count.toString())

        startDate?.let {
            options.putAll(
                mapOf(
                    "startDate" to formatDate(it)
                )
            )
        }

        endDate?.let {
            options.putAll(
                mapOf(
                    "endDate" to formatDate(it)
                )
            )
        }

        sortOrder?.let {
            options.putAll(
                mapOf(
                    "sortOrder" to it.toString()
                )
            )
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(AccountsApi::class.java, module)
        val response = service.listTransactions(accountIdKey, options).execute()

        return response.body()?.response
    }

    fun getTransaction(
        accountIdKey: String,
        transactionId: TransactionId
    ): Transaction? {
        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(AccountsApi::class.java, module)
        val response = service.getTransaction(accountIdKey, transactionId.toString()).execute()

        return response.body()?.response
    }

    fun viewPortfolio(
        accountIdKey: String,
        sortBy: PortfolioSortBy? = null,
        sortOrder: TransactionSortOrder? = null,
        marketSession: MarketSession? = null,
        totalsRequired: Boolean? = null,
        lotsRequired: Boolean? = null,
        count: Int? = null
    ): Portfolio? {

        val options: MutableMap<String, String> = mutableMapOf()

        sortBy?.let {
            options.put("sortBy", it.name)
        }

        sortOrder?.let {
            options.put("sortOrder", it.name)
        }

        sortOrder?.let {
            options.put("sortOrder", it.name)
        }

        marketSession?.let {
            options.put("marketSession", it.name)
        }

        totalsRequired?.let {
            options.put("totalsRequired", it.toString())
        }

        lotsRequired?.let {
            options.put("lotsRequired", it.toString())
        }

        count?.let {
            options.put("count", count.toString())
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer(false))

        val service = createClient(AccountsApi::class.java, module)
        val response = service.viewPortfolio(accountIdKey, options).execute()

        return response.body()?.response
    }
}
