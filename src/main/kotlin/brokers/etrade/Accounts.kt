package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
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

    /**
     * List user’s accounts
     * @sample com.seansoper.batil.samples.Accounts.list
     */
    fun list(): List<Account>? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getAccounts().execute()

        return response.body()?.response?.accountRoot?.accounts
    }

    // TODO: Document with sample
    fun getBalance(accountIdKey: String): AccountBalance? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getBalance(accountIdKey).execute()

        return response.body()?.response
    }

    /**
     * List transactions for an account
     * @param[accountIdKey] The unique account key
     * @param[startDate] The earliest date to include in the date range, history is available for two years
     * @param[endDate] The latest date to include in the date range
     * @param[sortOrder] Sort order for results
     * @param[startAt] Specifies the desired starting point of the set of items to return, used for paging
     * @param[count] Number of transactions to return in the response, defaults to 50, used for paging
     * @sample com.seansoper.batil.samples.Accounts.listTransactions
     */
    fun listTransactions(
        accountIdKey: String,
        startDate: GregorianCalendar? = null,
        endDate: GregorianCalendar? = null,
        sortOrder: TransactionSortOrder? = null,
        startAt: TransactionId? = null,
        count: Int? = null,
    ): TransactionResponse? {

        val options: MutableMap<String, String> = mutableMapOf()

        startDate?.let {
            options.put("startDate", formatDate(it))
        }

        endDate?.let {
            options.put("endDate", formatDate(it))
        }

        sortOrder?.let {
            options.put("sortOrder", it.toString())
        }

        startAt?.let {
            options.put("startAt", it.toString())
        }

        count?.let {
            options.put("count", it.toString())
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(AccountsApi::class.java, module)
        val response = service.listTransactions(accountIdKey, options).execute()

        return response.body()?.response
    }

    /**
     * List transactions for an account
     * @param[accountIdKey] The unique account key
     * @param[transactionId] [TransactionId] of the transaction to return
     * @sample com.seansoper.batil.samples.Accounts.getTransaction
     */
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

    /**
     * List transactions for an account
     * @param[accountIdKey] The unique account key
     * @param[sortBy] Field to sort by
     * @param[sortOrder] Sort order for results, defaults to [TransactionSortOrder.DESC]
     * @param[marketSession] Market session, defaults to [MarketSession.REGULAR]
     * @param[totalsRequired] Returns the total values of the portfolio, defaults to false
     * @param[lotsRequired] Returns lot positions of the portfolio, defaults to false
     * @param[count] Number of transactions to return in the response, defaults to 50, used for paging
     * @sample com.seansoper.batil.samples.Accounts.viewPortfolio
     */
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
