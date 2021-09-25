package com.seansoper.batil.brokers.etrade.api

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

enum class QuoteMode {
    REALTIME,
    DELAYED,
    CLOSING,

    /**
     * EH stands for Extended Hours aka after hours trading
     */
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

/**
 * @param[accountId] The user's account ID
 * @param[accountIdKey] The unique account key
 * @param[accountType] The account type
 * @param[institutionType] The institution type of the account
 * @param[closedDateRaw] The date when the account was closed
 * @param[mode] The account mode, i.e. cash or margin
 * @param[description] Description of account
 * @param[name] The nickname for the account
 * @param[status] The status of the account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
    val accountId: String?,
    val accountIdKey: String?,
    val accountType: AccountType?,
    val institutionType: String?,

    @JsonProperty("closedDate")
    val closedDateRaw: Int,

    @JsonProperty("accountMode")
    val mode: AccountMode?,

    @JsonProperty("accountDesc")
    val description: String?,

    @JsonProperty("accountName")
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

/**
 * @param[minEquityCall] The minimum equity call
 * @param[fedCall] The federal call
 * @param[cashCall] The cash call
 * @param[houseCall] The house call
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenCalls(
    val minEquityCall: Float?,
    val fedCall: Float?,
    val cashCall: Float?,
    val houseCall: Float?
)

/**
 * @param[totalAccountValue] The total account value
 * @param[netMv] The net market value
 * @param[netMvLong] The long net market value
 * @param[netMvShort] The short net market value
 * @param[totalLongValue] The total long value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RealTimeValues(
    val totalAccountValue: Float?,
    val netMv: Float?,
    val netMvLong: Float?,
    val netMvShort: Float?,
    val totalLongValue: Float?,
)

/**
 * @param[cashAvailableForInvestment] The cash available for investments
 * @param[cashAvailableForWithdrawal] The cash available for withdrawal
 * @param[totalAvailableForWithdrawal] The total amount available for withdrawal
 * @param[netCash] The net cash balance
 * @param[cashBalance] The current cash balance
 * @param[settledCashForInvestment] The settled cash for investments
 * @param[unSettledCashForInvestment] The unsettled cash for investments
 * @param[fundsWithheldFromPurchasePower] The funds withheld from the purchasing power
 * @param[fundsWithheldFromWithdrawal] The funds withheld from withdrawal
 * @param[marginBuyingPower] The margin account buying power
 * @param[cashBuyingPower] The cash account buying power
 * @param[dtMarginBuyingPower] The day trader margin account buying power
 * @param[dtCashBuyingPower] The day trader cash account buying power
 * @param[marginBalance] The margin account balance
 * @param[shortAdjustBalance] The short adjusted balance
 * @param[regtEquity] The Regulation T equity
 * @param[regtEquityPercent] The Regulation T equity percentage
 * @param[accountBalance] The current account balance
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ComputedBalance(
    val cashAvailableForInvestment: Float?,
    val cashAvailableForWithdrawal: Float?,
    val totalAvailableForWithdrawal: Float?,
    val netCash: Float?,
    val cashBalance: Float?,
    val settledCashForInvestment: Float?,
    val unSettledCashForInvestment: Float?,
    val fundsWithheldFromPurchasePower: Float?,
    val fundsWithheldFromWithdrawal: Float?,
    val marginBuyingPower: Float?,
    val cashBuyingPower: Float?,
    val dtMarginBuyingPower: Float?,
    val dtCashBuyingPower: Float?,
    val marginBalance: Float?,
    val shortAdjustBalance: Float?,
    val regtEquity: Float?,
    val regtEquityPercent: Float?,
    val accountBalance: Float?,

    @JsonProperty("OpenCalls")
    val openCalls: OpenCalls,

    @JsonProperty("RealTimeValues")
    val realTimeValues: RealTimeValues,
)

/**
 * @param[fundsForOpenOrdersCash] The funds reserved for open orders
 * @param[moneyMktBalance] The current cash balance of the money market or sweep deposit account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountCash(
    val fundsForOpenOrdersCash: Float?,
    val moneyMktBalance: Float?,
)

/**
 * @param[accountId] The account ID for which the balance is requested
 * @param[asOfDate] The effective date in UTC
 * @param[dayTraderStatus] The user's status as a day trader
 * @param[accountMode] The account mode indicating the account's special privileges as a cash account, a margin account, and so on
 * @param[accountType] The registered account type
 * @param[institutionType] The account institution type for which the balance or information is requested
 * @param[optionLevel] The option approval level of the account, expressed as a level 1 through 4 value
 * @param[quoteModeRaw] The quote type indicator
 * @param[description] The description of the account
 * @param[cash] Designates that account is a cash account
 * @param[balances] Designates the computed balance of the account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountBalance(
    val accountId: String?,
    val asOfDate: Int?,
    val dayTraderStatus: String?,
    val accountMode: AccountMode?,
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

/**
 * @param[symbol] The market symbol for the security being bought or sold
 * @param[type] The type of security being bought or sold
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductId(
    val symbol: String,

    @JsonProperty("typeCode")
    val type: ProductType
)

/**
 * @param[symbol] The symbol for which the quote details are being accessed
 * @param[expiryYear] The four-digit year the option will expire
 * @param[expiryMonth] The month (1-12) the option will expire
 * @param[expiryDay] The day (1-31) the option will expire
 * @param[expiryType] The expiration type for the option
 * @param[callPut] The option type
 * @param[securityType] The type code to identify the order or leg request
 * @param[securitySubType] The subtype of the security
 * @param[strikePrice] The strike price for the option
 * @param[productId] the corresponding [ProductId]
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Product(
    val symbol: String?,
    val expiryYear: Int?,
    val expiryMonth: Int?,
    val expiryDay: Int?,
    val expiryType: String?,
    val callPut: OptionType?,
    val securityType: SecurityType?,
    val securitySubType: String?,
    val strikePrice: Float?,
    val productId: ProductId?
) {
    val expiry: GregorianCalendar?
        get() {
            return if (expiryYear != null && expiryMonth != null && expiryDay != null) {
                if (expiryYear < 100) {

                    GregorianCalendar(expiryYear + 2000, expiryMonth, expiryDay)
                } else {
                    GregorianCalendar(expiryYear, expiryMonth, expiryDay)
                }
            } else {
                null
            }
        }
}

// Technically this is a Brokerage but the official documentation missed some add’l fields being returned
// https://apisb.etrade.com/docs/api/account/api-transaction-v1.html#/definitions/Brokerage

/**
 * @param[quantity] Item count; for example, share count
 * @param[price] Price per item if applicable; for example, price per share
 * @param[settlementCurrency] Settlement currency
 * @param[paymentCurrency] Payment currency
 * @param[fee] The brokerage fee
 * @param[displaySymbol] Display symbol of the security
 * @param[settlementDate] Settlement date of the transaction
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionTrade(
    val quantity: Float?,
    val price: Float?,
    val settlementCurrency: String?,
    val paymentCurrency: String?,
    val fee: Float?,
    val displaySymbol: String?,
    val settlementDate: Instant?,

    @JsonAlias("Product")
    val product: Product
)

/**
 * @param[transactionId] Numeric transaction ID
 * @param[accountId] Numeric account ID
 * @param[transactionDate] Date of the specified transaction
 * @param[postDate] The post date
 * @param[amount] Total cost of transaction, including commission if any
 * @param[description] The transaction description
 * @param[transactionType] Description of type of transaction i.e. "Sold Short"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Transaction(
    val transactionId: TransactionId,
    val accountId: String?,
    val transactionDate: Instant?,
    val postDate: Instant?,
    val amount: Float?,
    val description: String?,
    val transactionType: String?,

    @JsonProperty("brokerage")
    @JsonAlias("Brokerage")
    val trade: TransactionTrade,
)

/**
 * @param[marker] The starting point of the set of items returned.
 * @param[moreTransactions] Whether more transactions are available.
 * @param[transactionCount] The number of transactions returned
 * @param[totalCount] The total number of transactions available.
 * @param[transactions] The returned transactions.
 */
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

/**
 * @param[todaysGainLoss] Today's gain or loss
 * @param[todaysGainLossPct] Today's gain or loss percentage
 * @param[totalMarketValue] Today's market value
 * @param[totalGainLoss] The total gain or loss
 * @param[totalGainLossPct] The total gain loss percentage
 * @param[totalPricePaid] The total price paid
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PortfolioTotals(
    val todaysGainLoss: Float?,
    val todaysGainLossPct: Float?,
    val totalMarketValue: Float?,
    val totalGainLoss: Float?,
    val totalGainLossPct: Float?,
    val totalPricePaid: Float?,
)

/**
 * @param[change] The change
 * @param[changePct] The change percentage
 * @param[lastTrade] The last trade
 * @param[daysGain] The gain over the day
 * @param[totalGain] The total gain
 * @param[totalGainPct] The total gain percentage
 * @param[marketValue] The market value
 * @param[quoteStatus] REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
 * @param[lastTradeTime] The time of the last trade
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PerformanceView(
    val change: Float?,
    val changePct: Float?,
    val lastTrade: Float?,
    val daysGain: Float?,
    val totalGain: Float?,
    val totalGainPct: Float?,
    val marketValue: Float?,
    val quoteStatus: QuoteMode?,
    val lastTradeTime: Instant?
)

/**
 * @param[lastTrade] The last trade total
 * @param[lastTradeTime] The time of the last trade
 * @param[change] The change
 * @param[changePct] The change percentage
 * @param[peRatio] The Price to Earnings (P/E) ratio
 * @param[eps] The earnings per share
 * @param[dividend] The dividend
 * @param[divYield] The dividend yield
 * @param[marketCap] The market cap
 * @param[week52Range] The 52 week range
 * @param[quoteStatus] REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FundamentalView(
    val lastTrade: Float?,
    val lastTradeTime: Instant?,
    val change: Float?,
    val changePct: Float?,
    val peRatio: Float?,
    val eps: Float?,
    val dividend: Float?,
    val divYield: Float?,
    val marketCap: Float?,
    val week52Range: String?,
    val quoteStatus: QuoteMode?
)

/**
 *
 * @param[baseSymbolAndPrice] The price of the underlying or base symbol of the option
 * @param[premium] The option premium
 * @param[lastTrade] The last trade
 * @param[bid] The bid
 * @param[ask] The ask
 * @param[quoteStatus] REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
 * @param[lastTradeTime] The time of the last trade
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionsWatchView(
    val baseSymbolAndPrice: String?,
    val premium: Float?,
    val lastTrade: Float?,
    val bid: Float?,
    val ask: Float?,
    val quoteStatus: QuoteMode?,
    val lastTradeTime: Instant?,
)

/**
 * @param[lastTrade] The last trade
 * @param[lastTradeTime] The time of the last trade
 * @param[change] The change
 * @param[changePct] The change percentage
 * @param[volume] The total volume
 * @param[quoteStatus] REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
 * @param[sevenDayCurrentYield] The seven day current yield
 * @param[annualTotalReturn] The total annual return
 * @param[weightedAverageMaturity] The weighted average maturity
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class QuickView(
    val lastTrade: Float?,
    val lastTradeTime: Instant?,
    val change: Float?,
    val changePct: Float?,
    val volume: Int?,
    val quoteStatus: QuoteMode?,
    val sevenDayCurrentYield: Float?,
    val annualTotalReturn: Float?,
    val weightedAverageMaturity: Float?,
)

/**
 * @param[priceAdjustedFlag] The price adjusted flag
 * @param[price] The current market price
 * @param[adjPrice] The adjusted price
 * @param[change] The change
 * @param[changePct] The change percentage
 * @param[prevClose] The previous close
 * @param[adjPrevClose] The adjusted previous close
 * @param[volume] The volume
 * @param[lastTrade] The last trade
 * @param[lastTradeTime] The time of the last trade
 * @param[adjLastTrade] The adjusted last trade
 * @param[symbolDescription] The symbol description
 * @param[perform1Month] The one-month performance
 * @param[perform3Month] The three-month performance
 * @param[perform6Month] The six-month performance
 * @param[perform12Month] The 12-month performance
 * @param[prevDayVolume] The previous day's volume
 * @param[tenDayVolume] The 10 day average volume
 * @param[beta] The beta
 * @param[sv10DaysAvg] The 10 day average stochastic volatility
 * @param[sv20DaysAvg] The 20 day average stochastic volatility
 * @param[sv1MonAvg] The one month average stochastic volatility
 * @param[sv2MonAvg] The two month average stochastic volatility
 * @param[sv3MonAvg] The three month average stochastic volatility
 * @param[sv4MonAvg] The four month average stochastic volatility
 * @param[sv6MonAvg] The six month average stochastic volatility
 * @param[week52High] The 52 week high
 * @param[week52Low] The 52 week low
 * @param[week52Range] The 52 week range
 * @param[marketCap] The market cap
 * @param[daysRange] The day's range
 * @param[delta52WkHigh] The high for the 52 week high/low delta calculation
 * @param[delta52WkLow] The low for the 52 week high/low delta calculation
 * @param[currency] The currency
 * @param[exchange] The exchange
 * @param[marginable] The sum available for margin
 * @param[bid] The bid
 * @param[ask] The ask
 * @param[bidAskSpread] The bid ask spread
 * @param[bidSize] The size of the bid
 * @param[askSize] The size of the ask
 * @param[open] The open
 * @param[delta] The delta
 * @param[gamma] The gamma
 * @param[ivPct] The Implied Volatility (IV) percentage
 * @param[rho] The rho
 * @param[theta] The theta
 * @param[vega] The vega
 * @param[premium] The premium
 * @param[daysToExpiration] The days remaining until expiration
 * @param[intrinsicValue] The intrinsic value
 * @param[openInterest] The open interest
 * @param[optionsAdjustedFlag] The options adjusted flag
 * @param[deliverablesStr] The deliverables
 * @param[optionMultiplier] The option multiplier
 * @param[baseSymbolAndPrice] The price of the underlying or base symbol
 * @param[estEarnings] The estimated earnings
 * @param[eps] The earnings per share
 * @param[peRatio] The Price to Earnings (P/E) ratio
 * @param[annualDividend] The annual dividend
 * @param[dividend] The dividend
 * @param[divYield] The dividend yield
 * @param[divPayDate] The date of the dividend pay
 * @param[exDividendDate] The extended dividend date
 * @param[cusip] The CUSIP number
 * @param[quoteStatus] REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CompleteView(
    val priceAdjustedFlag: Boolean?,
    val price: Float?,
    val adjPrice: Float?,
    val change: Float?,
    val changePct: Float?,
    val prevClose: Float?,
    val adjPrevClose: Float?,
    val volume: Float?,
    val lastTrade: Float?,
    val lastTradeTime: Int?,
    val adjLastTrade: Float?,
    val symbolDescription: String?,
    val perform1Month: Float?,
    val perform3Month: Float?,
    val perform6Month: Float?,
    val perform12Month: Float?,
    val prevDayVolume: Int?,
    val tenDayVolume: Int?,
    val beta: Float?,
    val sv10DaysAvg: Float?,
    val sv20DaysAvg: Float?,
    val sv1MonAvg: Float?,
    val sv2MonAvg: Float?,
    val sv3MonAvg: Float?,
    val sv4MonAvg: Float?,
    val sv6MonAvg: Float?,
    val week52High: Float?,
    val week52Low: Float?,
    val week52Range: String?,
    val marketCap: Float?,
    val daysRange: String?,
    val delta52WkHigh: Float?,
    val delta52WkLow: Float?,
    val currency: String?,
    val exchange: String?,
    val marginable: Boolean?,
    val bid: Float?,
    val ask: Float?,
    val bidAskSpread: Float?,
    val bidSize: Int?,
    val askSize: Int?,
    val open: Float?,
    val delta: Float?,
    val gamma: Float?,
    val ivPct: Float?,
    val rho: Float?,
    val theta: Float?,
    val vega: Float?,
    val premium: Float?,
    val daysToExpiration: Int?,
    val intrinsicValue: Float?,
    val openInterest: Float?,
    val optionsAdjustedFlag: Boolean?,
    val deliverablesStr: String?,
    val optionMultiplier: Float?,
    val baseSymbolAndPrice: String?,
    val estEarnings: Float?,
    val eps: Float?,
    val peRatio: Float?,
    val annualDividend: Float?,
    val dividend: Float?,
    val divYield: Float?,
    val divPayDate: Int?,
    val exDividendDate: Int?,
    val cusip: String?,
    val quoteStatus: QuoteMode?,
)

/**
 * @param[positionId] The position ID
 * @param[positionLotId] The position lot ID
 * @param[price] The position lot price
 * @param[termCode] The term code
 * @param[daysGain] The days gain
 * @param[daysGainPct] The days gain percentage
 * @param[marketValue] The market value
 * @param[totalCost] The total cost
 * @param[totalCostForGainPct] The total cost for the percentage gain
 * @param[totalGain] The total gain
 * @param[lotSourceCode] The lot source code
 * @param[originalQty] The original quantity
 * @param[remainingQty] The remaining quantity
 * @param[availableQty] The available quantity
 * @param[orderNo] The order number
 * @param[legNo] The leg number
 * @param[acquiredDate] The date acquired
 * @param[locationCode] The location code
 * @param[exchangeRate] The exchange rate
 * @param[settlementCurrency] The settlement currency
 * @param[paymentCurrency] The payment currency
 * @param[adjPrice] The adjusted price
 * @param[commPerShare] The commissions per share
 * @param[feesPerShare] The fees per share
 * @param[premiumAdj] The adjusted premium
 * @param[shortType] The short type
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PositionLot(
    val positionId: Int?,
    val positionLotId: Int?,
    val price: Float?,
    val termCode: Int?,
    val daysGain: Float?,
    val daysGainPct: Float?,
    val marketValue: Float?,
    val totalCost: Float?,
    val totalCostForGainPct: Float?,
    val totalGain: Float?,
    val lotSourceCode: Int?,
    val originalQty: Float?,
    val remainingQty: Float?,
    val availableQty: Float?,
    val orderNo: Int?,
    val legNo: Int?,
    val acquiredDate: Int?,
    val locationCode: Int?,
    val exchangeRate: Float?,
    val settlementCurrency: String?,
    val paymentCurrency: String?,
    val adjPrice: Float?,
    val commPerShare: Float?,
    val feesPerShare: Float?,
    val premiumAdj: Float?,
    val shortType: Int?,
)

/**
 * @param[positionId] The position ID
 * @param[accountId] Numeric account ID
 * @param[product] The product
 * @param[osiKey] The Options Symbology Initiative (OSI) key containing the option root symbol, expiration date, call/put indicator, and strike price
 * @param[symbolDescription] The symbol description
 * @param[dateAcquired] The date the position was acquired
 * @param[pricePaid] The price paid for the position
 * @param[price] The price of the position
 * @param[commissions] The commissions paid for the position
 * @param[otherFees] The other fees paid to acquire the position
 * @param[quantity] The quantity
 * @param[positionIndicator] The position indicator
 * @param[positionType] The position type
 * @param[change] The change
 * @param[changePct] The percentage change
 * @param[daysGain] The day's gain
 * @param[daysGainPct] The percentage day's gain
 * @param[marketValue] The market value
 * @param[totalCost] The total cost
 * @param[totalGain] The total gain
 * @param[totalGainPct] The total gain percentage
 * @param[pctOfPortfolio] The percentage of the portfolio
 * @param[costPerShare] The cost per share
 * @param[todayCommissions] Today's total commissions
 * @param[todayFees] Today's total fees
 * @param[todayPricePaid] Today's total price paid
 * @param[todayQuantity] Today's total quantity
 * @param[quotestatus] The quote type
 * @param[dateTimeUTC] The date and time in UTC
 * @param[adjPrevClose] The previous adjusted close
 * @param[performance] The performance view
 * @param[fundamental] The fundamental view
 * @param[optionsWatch] The options watch view
 * @param[quick] The quick view
 * @param[complete] The complete view
 * @param[lotsDetails] The lots details
 * @param[quoteDetails] The quote details
 * @param[positionLot] The position lot
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PortfolioPosition(
    val positionId: Long,
    val accountId: String?,
    @JsonProperty("Product")
    val product: Product?,
    val osiKey: String?,
    val symbolDescription: String?,
    val dateAcquired: Instant?,
    val pricePaid: Float?,
    val price: Float?,
    val commissions: Float?,
    val otherFees: Float?,
    val quantity: Float?,
    val positionIndicator: PositionIndicatorType?,
    val positionType: String?,
    val change: Float?,
    val changePct: Float?,
    val daysGain: Float?,
    val daysGainPct: Float?,
    val marketValue: Float?,
    val totalCost: Float?,
    val totalGain: Float?,
    val totalGainPct: Float?,
    val pctOfPortfolio: Float?,
    val costPerShare: Float?,
    val todayCommissions: Float?,
    val todayFees: Float?,
    val todayPricePaid: Float?,
    val todayQuantity: Float?,
    val quotestatus: String?,
    val dateTimeUTC: Int?,
    val adjPrevClose: Float?,
    val performance: PerformanceView?,
    val fundamental: FundamentalView?,
    val optionsWatch: OptionsWatchView?,
    @JsonProperty("Quick")
    val quick: QuickView?,
    val complete: CompleteView?,
    val lotsDetails: String?,
    val quoteDetails: String?,
    val positionLot: List<PositionLot>?
)

/**
 * @param[accountId] Numeric account ID
 * @param[next] The next account portfolio item
 * @param[totalPages] The total number of pages
 * @param[nextPageNo] The next page number
 * @param[positions] The positions in this portfolio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountPortfolio(
    val accountId: String,
    val next: String?,
    val totalPages: Int?,
    val nextPageNo: String?,
    @JsonProperty("Position")
    val positions: List<PortfolioPosition>
)

/**
 * @param[totals] The portfolio totals
 * @param[accountPortfolio] List of account portfolios
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Portfolio(
    val totals: PortfolioTotals?,
    @JsonProperty("AccountPortfolio")
    val accountPortfolio: List<AccountPortfolio>
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
