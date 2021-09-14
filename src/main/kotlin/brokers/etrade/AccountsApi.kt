package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.time.Instant
import java.util.GregorianCalendar

enum class AccountMode {
    CASH, MARGIN
}

enum class AccountStatus {
    ACTIVE, CLOSED
}

enum class AccountType {
    AMMCHK, ARO, BCHK, BENFIRA, BENFROTHIRA, BENF_ESTATE_IRA, BENF_MINOR_IRA, BENF_ROTH_ESTATE_IRA, BENF_ROTH_MINOR_IRA, BENF_ROTH_TRUST_IRA, BENF_TRUST_IRA, BRKCD, BROKER, CASH, C_CORP, CONTRIBUTORY, COVERDELL_ESA, CONVERSION_ROTH_IRA, CREDITCARD, COMM_PROP, CONSERVATOR, CORPORATION, CSA, CUSTODIAL, DVP, ESTATE, EMPCHK, EMPMMCA, ETCHK, ETMMCHK, HEIL, HELOC, INDCHK, INDIVIDUAL, INDIVIDUAL_K, INVCLUB, INVCLUB_C_CORP, INVCLUB_LLC_C_CORP, INVCLUB_LLC_PARTNERSHIP, INVCLUB_LLC_S_CORP, INVCLUB_PARTNERSHIP, INVCLUB_S_CORP, INVCLUB_TRUST, IRA_ROLLOVER, JOINT, JTTEN, JTWROS, LLC_C_CORP, LLC_PARTNERSHIP, LLC_S_CORP, LLP, LLP_C_CORP, LLP_S_CORP, IRA, IRACD, MONEY_PURCHASE, MARGIN, MRCHK, MUTUAL_FUND, NONCUSTODIAL, NON_PROFIT, OTHER, PARTNER, PARTNERSHIP, PARTNERSHIP_C_CORP, PARTNERSHIP_S_CORP, PDT_ACCOUNT, PM_ACCOUNT, PREFCD, PREFIRACD, PROFIT_SHARING, PROPRIETARY, REGCD, ROTHIRA, ROTH_INDIVIDUAL_K, ROTH_IRA_MINORS, SARSEPIRA, S_CORP, SEPIRA, SIMPLE_IRA, TIC, TRD_IRA_MINORS, TRUST, VARCD, VARIRACD
}

enum class InstitutionType {
    BROKERAGE, GLOBALTRADING, NONUS, STOCKPLAN, LENDING, HELOC, HEIL, ONTRACK, GENPACT, AUTO, AUTOLOAN, BETA, LOYALTY, SBASKET, CC_BALANCETRANSFER, GENPACT_LEAD, GANIS, MORTGAGE, EXTERNAL, FUTURES, VISA, RJO, WDBH
}

enum class OptionLevel {
    NO_OPTIONS, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4
}

// EH = Extended Hours = AHT = After Hours Trading
enum class QuoteMode {
    REALTIME,
    DELAYED,
    CLOSING,
    EH_REALTIME,
    EH_BEFORE_OPEN,
    EH_CLOSING,
    NONE
}

enum class ProductType {
    EQUITY, OPTION, MUTUAL_FUND, INDEX, MONEY_MARKET_FUND, BOND, UNKNOWN, WILDCARD, MOVE, ETF, EQUITY_OPTION_ETF, EQUITY_ETF, CLOSED_END_FUND, PREFERRED, EQUITY_OPTN, EXCHANGE_TRADED_FUND, MUTUAL_FUND_MONEY_MARKET_FUND
}

enum class PositionIndicatorType {
    TYPE1, TYPE2, TYPE5, UNDEFINED
}

enum class SecurityType {
    BOND, EQ, INDX, MF, MMF, OPTN;

    val description: String
        get() {
            return when (this) {
                BOND -> "Bond"
                EQ -> "Equity"
                INDX -> "Index"
                MF -> "Mutual Fund"
                MMF -> "Managed Mutual Fund"
                OPTN -> "Option"
            }
        }
}

typealias TransactionId = Long

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountRoot(
    @JsonProperty("Accounts")
    val accountRoot: AccountList
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountList(
    @JsonProperty("Account")
    val accounts: List<Account>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
    val accountId: String?, // The user's account ID
    val accountIdKey: String?, // The unique account key
    val accountType: AccountType?, // The account type
    val institutionType: String?, // BROKERAGE

    @JsonProperty("closedDate")
    val closedDateRaw: Int, // The date when the account was closed

    @JsonProperty("accountMode")
    val mode: AccountMode?,

    @JsonProperty("accountDesc") // Description of account
    val description: String?,

    @JsonProperty("accountName") // The nickname for the account
    val name: String?,

    @JsonProperty("accountStatus")
    val status: AccountStatus?
) {
    val dateClosed: Instant?
        get() {
            return if (closedDateRaw > 0) {
                Instant.ofEpochSecond(closedDateRaw.toLong())
            } else {
                null
            }
        }

    val closed: Boolean
        get() {
            return status == AccountStatus.CLOSED
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountListResponse(
    @JsonProperty("AccountListResponse")
    val response: AccountRoot
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenCalls(
    val minEquityCall: Float?, // The minimum equity call
    val fedCall: Float?, // The federal call
    val cashCall: Float?, // The cash call
    val houseCall: Float? // The house call
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RealTimeValues(
    val totalAccountValue: Float?, // The total account value
    val netMv: Float?, // The net market value
    val netMvLong: Float?, // The long net market value
    val netMvShort: Float?, // The short net market value
    val totalLongValue: Float?, // The total long value
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ComputedBalance(
    val cashAvailableForInvestment: Float?, // The cash available for investments
    val cashAvailableForWithdrawal: Float?, // The cash available for withdrawal
    val totalAvailableForWithdrawal: Float?, // The total amount available for withdrawal
    val netCash: Float?, // The net cash balance
    val cashBalance: Float?, // The current cash balance
    val settledCashForInvestment: Float?, // The settled cash for investments
    val unSettledCashForInvestment: Float?, // The unsettled cash for investments
    val fundsWithheldFromPurchasePower: Float?, // The funds withheld from the purchasing power
    val fundsWithheldFromWithdrawal: Float?, // The funds withheld from withdrawal
    val marginBuyingPower: Float?, // The margin account buying power
    val cashBuyingPower: Float?, // The cash account buying power
    val dtMarginBuyingPower: Float?, // The day trader margin account buying power
    val dtCashBuyingPower: Float?, // The day trader cash account buying power
    val marginBalance: Float?, // The margin account balance
    val shortAdjustBalance: Float?, // The short adjusted balance
    val regtEquity: Float?, // The Regulation T equity
    val regtEquityPercent: Float?, // The Regulation T equity percentage
    val accountBalance: Float?, // The current account balance

    @JsonProperty("OpenCalls")
    val openCalls: OpenCalls,

    @JsonProperty("RealTimeValues")
    val realTimeValues: RealTimeValues,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountCash(
    val fundsForOpenOrdersCash: Float?, // The funds reserved for open orders
    val moneyMktBalance: Float?, // The current cash balance of the money market or sweep deposit account
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountBalance(
    val accountId: String?, // The account ID for which the balance is requested
    val asOfDate: Int?, // The effective date in UTC
    val dayTraderStatus: String?, // The user's status as a day trader
    val accountMode: AccountMode?, // The account mode indicating the account's special privileges as a cash account, a margin account, and so on
    val accountType: AccountType?,
    val institutionType: InstitutionType?,
    val optionLevel: OptionLevel,

    @JsonProperty("quoteMode")
    val quoteModeRaw: Int,

    @JsonProperty("accountDescription")
    val description: String?,

    @JsonProperty("Cash")
    val cash: AccountCash,

    @JsonProperty("Computed")
    val balances: ComputedBalance
) {
    val quoteStatus: QuoteMode
        get() {
            return QuoteMode.values()[quoteModeRaw]
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BalanceResponse(
    @JsonProperty("BalanceResponse")
    val response: AccountBalance
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Product(
    val symbol: String, // The market symbol for the security being bought or sold

    @JsonProperty("typeCode")
    val type: ProductType
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionStrike(
    val symbol: String?, // The symbol for which the quote details are being accessed
    val expiryYear: Int?, // The four-digit year the option will expire
    val expiryMonth: Int?, // The month (1-12) the option will expire
    val expiryDay: Int?, // The day (1-31) the option will expire
    val expiryType: String?, // The expiration type for the option
    val callPut: OptionType?, // The option type
    val securityType: SecurityType?, // The type code to identify the order or leg request
    val securitySubType: String?, // The subtype of the security

    @JsonProperty("productId")
    val product: Product?,

    @JsonProperty("strikePrice")
    val price: Float?, // The strike price for the option
) {
    val expiry: GregorianCalendar?
        get() {
            return if (expiryYear != null && expiryMonth != null && expiryDay != null) {
                GregorianCalendar(expiryYear + 2000, expiryMonth, expiryDay)
            } else {
                null
            }
        }
}

// Corresponds to Brokerage
// https://apisb.etrade.com/docs/api/account/api-transaction-v1.html#/definitions/Brokerage
@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionTrade(
    val quantity: Float?, // Item count; for example, share count
    val price: Float?, // Price per item if applicable; for example, price per share
    val settlementCurrency: String?, // Settlement currency
    val paymentCurrency: String?, // Payment currency
    val fee: Float?, // The brokerage fee
    val displaySymbol: String?,
    val settlementDate: Instant?,

    @JsonProperty("product")
    @JsonAlias("Product")
    val strike: TransactionStrike
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transaction(
    val transactionId: TransactionId,
    val accountId: String?, // Numeric account ID
    val transactionDate: Instant?, // Date of the specified transaction
    val postDate: Instant?, // The post date
    val amount: Float?, // Total cost of transaction, including commission if any
    val description: String?, // The transaction description
    val transactionType: String?, // Description of type of transaction i.e. "Sold Short"

    @JsonProperty("brokerage")
    @JsonAlias("Brokerage")
    val trade: TransactionTrade, // The brokerage involved in the transaction
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionResponse(
    val marker: String?,
    val moreTransactions: Boolean,
    val transactionCount: Int,
    val totalCount: Int,

    @JsonProperty("Transaction")
    val transactions: List<Transaction>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionListResponse(
    @JsonProperty("TransactionListResponse")
    val response: TransactionResponse
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionDetailsResponse(
    @JsonProperty("TransactionDetailsResponse")
    val response: Transaction
)

// Portfolio
@JsonIgnoreProperties(ignoreUnknown = true)
data class PortfolioTotals(
    val todaysGainLoss: Float?, // Today's gain or loss
    val todaysGainLossPct: Float?, // Today's gain or loss percentage
    val totalMarketValue: Float?, // Today's market value
    val totalGainLoss: Float?, // The total gain or loss
    val totalGainLossPct: Float?, // The total gain loss percentage
    val totalPricePaid: Float?, // The total price paid
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerformanceView(
    val change: Float?, // The change
    val changePct: Float?, // The change percentage
    val lastTrade: Float?, // The last trade
    val daysGain: Float?, // The gain over the day
    val totalGain: Float?, // The total gain
    val totalGainPct: Float?, // The total gain percentage
    val marketValue: Float?, // The market value
    val quoteStatus: QuoteMode?, // REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    val lastTradeTime: Instant? // The time of the last trade
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FundamentalView(
    val lastTrade: Float?, // The last trade total
    val lastTradeTime: Instant?, // The time of the last trade
    val change: Float?, // The change
    val changePct: Float?, // The change percentage
    val peRatio: Float?, // The Price to Earnings (P/E) ratio
    val eps: Float?, // The earnings per share
    val dividend: Float?, // The dividend
    val divYield: Float?, // The dividend yield
    val marketCap: Float?, // The market cap
    val week52Range: String?, // The 52 week range
    val quoteStatus: QuoteMode? // REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionsWatchView(
    val baseSymbolAndPrice: String?, // The price of the underlying or base symbol of the option
    val premium: Float?, // The option premium
    val lastTrade: Float?, // The last trade
    val bid: Float?, // The bid
    val ask: Float?, // The ask
    val quoteStatus: QuoteMode?, // REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    val lastTradeTime: Instant?, // The time of the last trade
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuickView(
    val lastTrade: Float?, // The last trade
    val lastTradeTime: Instant?, // The time of the last trade
    val change: Float?, // The change
    val changePct: Float?, // The change percentage
    val volume: Int?, // The total volume
    val quoteStatus: QuoteMode?, // REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    val sevenDayCurrentYield: Float?, // The seven day current yield
    val annualTotalReturn: Float?, // The total annual return
    val weightedAverageMaturity: Float?, // The weighted average maturity
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompleteView(
    val priceAdjustedFlag: Boolean?, // The price adjusted flag
    val price: Float?, // The current market price
    val adjPrice: Float?, // The adjusted price
    val change: Float?, // The change
    val changePct: Float?, // The change percentage
    val prevClose: Float?, // The previous close
    val adjPrevClose: Float?, // The adjusted previous close
    val volume: Float?, // The volume
    val lastTrade: Float?, // The last trade
    val lastTradeTime: Int?, // The time of the last trade
    val adjLastTrade: Float?, // The adjusted last trade
    val symbolDescription: String?, // The symbol description
    val perform1Month: Float?, // The one-month performance
    val perform3Month: Float?, // The three-month performance
    val perform6Month: Float?, // The six-month performance
    val perform12Month: Float?, // The 12-month performance
    val prevDayVolume: Int?, // The previous day's volume
    val tenDayVolume: Int?, // The 10 day average volume
    val beta: Float?, // The beta
    val sv10DaysAvg: Float?, // The 10 day average stochastic volatility
    val sv20DaysAvg: Float?, // The 20 day average stochastic volatility
    val sv1MonAvg: Float?, // The one month average stochastic volatility
    val sv2MonAvg: Float?, // The two month average stochastic volatility
    val sv3MonAvg: Float?, // The three month average stochastic volatility
    val sv4MonAvg: Float?, // The four month average stochastic volatility
    val sv6MonAvg: Float?, // The six month average stochastic volatility
    val week52High: Float?, // The 52 week high
    val week52Low: Float?, // The 52 week low
    val week52Range: String?, // The 52 week range
    val marketCap: Float?, // The market cap
    val daysRange: String?, // The day's range
    val delta52WkHigh: Float?, // The high for the 52 week high/low delta calculation
    val delta52WkLow: Float?, // The low for the 52 week high/low delta calculation
    val currency: String?, // The currency
    val exchange: String?, // The exchange
    val marginable: Boolean?, // The sum available for margin
    val bid: Float?, // The bid
    val ask: Float?, // The ask
    val bidAskSpread: Float?, // The bid ask spread
    val bidSize: Int?, // The size of the bid
    val askSize: Int?, // The size of the ask
    val open: Float?, // The open
    val delta: Float?, // The delta
    val gamma: Float?, // The gamma
    val ivPct: Float?, // The Implied Volatility (IV) percentage
    val rho: Float?, // The rho
    val theta: Float?, // The theta
    val vega: Float?, // The vega
    val premium: Float?, // The premium
    val daysToExpiration: Int?, // The days remaining until expiration
    val intrinsicValue: Float?, // The intrinsic value
    val openInterest: Float?, // The open interest
    val optionsAdjustedFlag: Boolean?, // The options adjusted flag
    val deliverablesStr: String?, // The deliverables
    val optionMultiplier: Float?, // The option multiplier
    val baseSymbolAndPrice: String?, // The price of the underlying or base symbol
    val estEarnings: Float?, // The estimated earnings
    val eps: Float?, // The earnings per share
    val peRatio: Float?, // The Price to Earnings (P/E) ratio
    val annualDividend: Float?, // The annual dividend
    val dividend: Float?, // The dividend
    val divYield: Float?, // The dividend yield
    val divPayDate: Int?, // The date of the dividend pay
    val exDividendDate: Int?, // The extended dividend date
    val cusip: String?, // The CUSIP number
    val quoteStatus: QuoteMode?, // REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PositionLot(
    val positionId: Int?, // The position ID
    val positionLotId: Int?, // The position lot ID
    val price: Float?, // The position lot price
    val termCode: Int?, // The term code
    val daysGain: Float?, // The days gain
    val daysGainPct: Float?, // The days gain percentage
    val marketValue: Float?, // The market value
    val totalCost: Float?, // The total cost
    val totalCostForGainPct: Float?, // The total cost for the percentage gain
    val totalGain: Float?, // The total gain
    val lotSourceCode: Int?, // The lot source code
    val originalQty: Float?, // The original quantity
    val remainingQty: Float?, // The remaining quantity
    val availableQty: Float?, // The available quantity
    val orderNo: Int?, // The order number
    val legNo: Int?, // The leg number
    val acquiredDate: Int?, // The date acquired
    val locationCode: Int?, // The location code
    val exchangeRate: Float?, // The exchange rate
    val settlementCurrency: String?, // The settlement currency
    val paymentCurrency: String?, // The payment currency
    val adjPrice: Float?, // The adjusted price
    val commPerShare: Float?, // The commissions per share
    val feesPerShare: Float?, // The fees per share
    val premiumAdj: Float?, // The adjusted premium
    val shortType: Int?, // The short type
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PortfolioPosition(
    val positionId: Long, // The position ID
    val accountId: String?, // Numeric account ID
    @JsonProperty("Product")
    val product: TransactionStrike?, // The product
    val osiKey: String?, // The Options Symbology Initiative (OSI) key containing the option root symbol, expiration date, call/put indicator, and strike price
    val symbolDescription: String?, // The symbol description
    val dateAcquired: Instant?, // The date the position was acquired
    val pricePaid: Float?, // The price paid for the position
    val price: Float?, // The price of the position
    val commissions: Float?, // The commissions paid for the position
    val otherFees: Float?, // The other fees paid to acquire the position
    val quantity: Float?, // The quantity
    val positionIndicator: PositionIndicatorType?,
    val positionType: String?, // The position type
    val change: Float?, // The change
    val changePct: Float?, // The percentage change
    val daysGain: Float?, // The day's gain
    val daysGainPct: Float?, // The percentage day's gain
    val marketValue: Float?, // The market value
    val totalCost: Float?, // The total cost
    val totalGain: Float?, // The total gain
    val totalGainPct: Float?, // The total gain percentage
    val pctOfPortfolio: Float?, // The percentage of the portfolio
    val costPerShare: Float?, // The cost per share
    val todayCommissions: Float?, // Today's total commissions
    val todayFees: Float?, // Today's total fees
    val todayPricePaid: Float?, // Today's total price paid
    val todayQuantity: Float?, // Today's total quantity
    val quotestatus: String?, // The quote type
    val dateTimeUTC: Int?, // The date and time in UTC
    val adjPrevClose: Float?, // The previous adjusted close
    val performance: PerformanceView?, // The performance view
    val fundamental: FundamentalView?, // The fundamental view
    val optionsWatch: OptionsWatchView?, // The options watch view
    @JsonProperty("Quick")
    val quick: QuickView?, // The quick view
    val complete: CompleteView?, // The complete view
    val lotsDetails: String?, // The lots details
    val quoteDetails: String?, // The quote details
    val positionLot: List<PositionLot>? // The position lot
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountPortfolio(
    val accountId: String, // Numeric account ID
    val next: String?, // The next account portfolio item
    val totalPages: Int?, // The total number of pages
    val nextPageNo: String?, // The next page number
    @JsonProperty("Position")
    val positions: List<PortfolioPosition>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Portfolio(
    val totals: PortfolioTotals?, // The portfolio totals
    @JsonProperty("AccountPortfolio")
    val accountPortfolio: List<AccountPortfolio> // The account portfolio array
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PortfolioResponse(
    @JsonProperty("PortfolioResponse")
    val response: Portfolio
)

interface AccountsApi {

    @GET("v1/accounts/list")
    fun getAccounts(): Call<AccountListResponse>

    @GET("/v1/accounts/{accountIdKey}/balance?instType=BROKERAGE&realTimeNAV=true")
    fun getBalance(@Path("accountIdKey") accountIdKey: String): Call<BalanceResponse>

    @GET("/v1/accounts/{accountIdKey}/transactions")
    fun listTransactions(
        @Path("accountIdKey") accountIdKey: String,
        @QueryMap options: Map<String, String>
    ): Call<TransactionListResponse>

    @GET("/v1/accounts/{accountIdKey}/transactions/{transactionId}")
    fun getTransaction(
        @Path("accountIdKey") accountIdKey: String,
        @Path("transactionId") transactionId: String
    ): Call<TransactionDetailsResponse>

    @GET("/v1/accounts/{accountIdKey}/portfolio")
    fun viewPortfolio(
        @Path("accountIdKey") accountIdKey: String,
        @QueryMap options: Map<String, String>
    ): Call<PortfolioResponse>
}
