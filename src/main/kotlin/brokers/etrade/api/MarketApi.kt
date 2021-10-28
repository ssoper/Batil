package com.seansoper.batil.brokers.etrade.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

enum class MessageType {
    WARNING, INFO, INFO_HOLD, ERROR
}

enum class OptionCategory {
    STANDARD, ALL, MINI
}

enum class OptionType {
    CALL, PUT, CALLPUT
}

enum class OptionExpirationType {
    UNSPECIFIED, DAILY, WEEKLY, MONTHLY, QUARTERLY, VIX, ALL, MONTHEND
}

/**
 * @param[description] The text of the result message, indicating order status, success or failure, additional requirements that must be met before placing the order, and so on. Applications typically display this message to the user, which may result in further user action.
 * @param[code] The standard numeric code of the result message. Refer to the Error Messages documentation for examples. May optionally be displayed to the user, but is primarily intended for internal use.
 * @param[type] The type used to identify the message
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
    val description: String,
    val code: Int,
    val type: MessageType
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessagesResponse(
    @JsonProperty("Message")
    val messages: List<Message>
)

// TODO: This is very similar to AccountsApi.QuoteMode and should probably be merged with it
enum class QuoteStatus {
    REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED, UNKNOWN
}

// TODO: Implement optionDeliverableList
// TODO: Implement ehQuote

/**
 * @param[adjustedFlag] Indicates whether an option has been adjusted due to a corporate action (for example, a dividend or stock split)
 * @param[annualDividend] Cash amount paid per share over the past year
 * @param[ask] The current ask price for a security
 * @param[askExchange] Code for the exchange reporting the ask price
 * @param[askSize] Number shares or contracts offered by broker or dealer at the ask price
 * @param[askTime] The time of the ask; for example, ’15:15:43 PDT 03-21-2018’
 * @param[bid] Current bid price for a security
 * @param[bidExchange] Code for the exchange reporting the bid price
 * @param[bidSize] Number of shares or contracts offered at the bid price
 * @param[bidTime] Time of the bid; for example ’15:15:43 PDT 03-21-2018’
 * @param[changeClose] Dollar change of the last price from the previous close
 * @param[changeClosePercentage] Percentage change of the last price from the previous close
 * @param[companyName] Name of the company or mutual fund (shows up to 40 characters)
 * @param[daysToExpiration] Number of days before the option expires
 * @param[dirLast] Direction of movement; that is, whether the current price is higher or lower than the price of the most recent trade
 * @param[dividend] Cash amount per share of the latest dividend
 * @param[eps] Earnings per share on rolling basis (stocks only)
 * @param[estEarnings] Projected Earnings per share for the next fiscal year (stocks only)
 * @param[exDividendDate] Date (in Epoch time) on which shareholders were entitled to receive the latest dividend
 * @param[exchgLastTrade] Code for the exchange of the last trade
 * @param[fsi] Financial Status Indicator indicates whether a Nasdaq-listed issuer has failed to submit its regulatory filings on timely basis, failed to meet continuing listing standards, and/or filed for bankruptcy. Codes are: D - Deficient, E - Delinquent, Q - Bankrupt, N - Normal, G - Deficient and Bankrupt, H - Deficient and Delinquent, J - Delinquent and Bankrupt, and K - Deficient, Delinquent, and Bankrupt.
 * @param[high] Highest price at which a security has traded during the current day
 * @param[high52] Highest price at which a security has traded during the past year (52 weeks). For options, this value is the lifetime high.
 * @param[highAsk] Highest ask price for the current trading day
 * @param[highBid] Highest bid price for the current trading day
 * @param[lastTrade] Price of the most recent trade of a security
 * @param[low] Lowest price at which a security has traded during the current day
 * @param[low52] Lowest price at which security has traded during the past year (52 weeks). For options, this value is the lifetime low.
 * @param[lowAsk] Lowest ask price for the current trading day
 * @param[lowBid] Lowest bid price for the current trading day
 * @param[numberOfTrades] Number of transactions involving buying a security from another entity
 * @param[open] Price of a security at the current day’s market open
 * @param[openInterest] Total number of options or futures contracts that are not closed or delivered on a particular day
 * @param[optionStyle] Specifies how the contract treats the expiration date. Possible values are "European" (options can be exercised only on the expiration date) or "American" (options can be exercised any time before they expire).
 * @param[optionUnderlier] Symbol for the underlier (options only)
 * @param[optionUnderlierExchange] Exchange code for option underlier symbol; applicable only for options
 * @param[previousClose] Official price at the close of the previous trading day
 * @param[previousDayVolume] Final volume from the previous market session
 * @param[primaryExchange] Exchange code of the primary listing exchange for this instrument
 * @param[symbolDescription] Description of the security; for example, the company name or the option description
 * @param[todayClose] Price at the close of the regular trading session for the current day
 * @param[totalVolume] Total number of shares or contracts exchanging hands
 * @param[upc] Uniform Practice Code identifies specific FINRA advisories detailing unusual circumstances; for example, extremely large dividends, when-issued settlement dates, and worthless securities
 * @param[volume10Day] Ten-day average trading volume for the security
 * @param[cashDeliverable] The cash deliverables in case of multiple deliverables
 * @param[marketCap] The value market capitalization
 * @param[sharesOutstanding] The number of outstanding shares
 * @param[nextEarningDate] If requireEarningsDate is true, the next earning date value in mm/dd/yyyy format
 * @param[beta] A measure of a stock’s volatility relative to the primary market index
 * @param[yield] The dividend yield
 * @param[declaredDividend] The declared dividend
 * @param[dividendPayableDate] The dividend payable date
 * @param[pe] The option multiplier
 * @param[marketCloseBidSize] The market close bid size
 * @param[marketCloseAskSize] The market close ask size
 * @param[marketCloseVolume] The market close volume
 * @param[week52LowDate] The date at which the price was the lowest in the last 52 weeks; applicable for stocks and mutual funds
 * @param[week52HiDate] The date at which the price was highest in the last 52 weeks; applicable for stocks and mutual funds
 * @param[intrinsicValue] The intrinsic value of the share
 * @param[timePremium] The value of the time premium
 * @param[optionMultiplier] The option multiplier value
 * @param[contractSize] CThe contract size of the option
 * @param[expirationDate] The expiration date of the option
 * @param[optionPreviousBidPrice] The option previous bid price
 * @param[optionPreviousAskPrice] OThe option previous ask price
 * @param[osiKey] The Options Symbology Initiative (OSI) representation of the option symbol
 * @param[timeOfLastTrade] The time when the last trade was placed
 * @param[averageVolume] Average volume value corresponding to the symbol
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TickerData(
    val adjustedFlag: Boolean?,
    val annualDividend: Float?,
    val ask: Float?,
    val askExchange: String?,
    val askSize: Int?,
    val askTime: String?,
    val bid: Float?,
    val bidExchange: String?,
    val bidSize: Int?,
    val bidTime: String?,
    val changeClose: Float?,
    val changeClosePercentage: Float?,
    val companyName: String?,
    val daysToExpiration: Int?,
    val dirLast: String?,
    val dividend: Float?,
    val eps: Float?,
    val estEarnings: Float?,
    val exDividendDate: Int?,
    val exchgLastTrade: String?,
    val fsi: String?,
    val high: Float?,
    val high52: Float?,
    val highAsk: Float?,
    val highBid: Float?,
    val lastTrade: Float?,
    val low: Float?,
    val low52: Float?,
    val lowAsk: Float?,
    val lowBid: Float?,
    val numberOfTrades: Int?,
    val open: Float?,
    val openInterest: Int?,
    val optionStyle: String?,
    val optionUnderlier: String?,
    val optionUnderlierExchange: String?,
    val previousClose: Float?,
    val previousDayVolume: Int?,
    val primaryExchange: String?,
    val symbolDescription: String?,
    val todayClose: Float?,
    val totalVolume: Int?,
    val upc: Int?,
    val volume10Day: Int?,
    val cashDeliverable: Float?,
    val marketCap: Float?,
    val sharesOutstanding: Float?,
    val nextEarningDate: String?,
    val beta: Float?,
    val yield: Float?,
    val declaredDividend: Float?,
    val dividendPayableDate: Int?,
    val pe: Float?,
    val marketCloseBidSize: Int?,
    val marketCloseAskSize: Int?,
    val marketCloseVolume: Int?,
    val week52LowDate: Int?,
    val week52HiDate: Int?,
    val intrinsicValue: Float?,
    val timePremium: Float?,
    val optionMultiplier: Float?,
    val contractSize: Float?,
    val expirationDate: Int?,
    val optionPreviousBidPrice: Float?,
    val optionPreviousAskPrice: Float?,
    val osiKey: String?,
    val timeOfLastTrade: Int?,
    val averageVolume: Int?,
)

// TODO: Documentation shows add’l fields to be implemented The quote details to be displayed. This field depends on the detailFlag input parameter. For example, if detailFlag is ALL, AllQuoteDetails are displayed. If detailFlag is MF_DETAIL, the MutualFund structure gets displayed.

/**
 * @param[dateTime] The date and time of the quote
 * @param[quoteStatus] The status of the quote
 * @param[ahFlag] Indicates whether the quote details are being displayed after hours or not
 * @param[symbol] Ticker symbol
 * @param[tickerData] The quote details to be displayed. This field depends on the detailFlag input parameter. For example, if detailFlag is ALL, AllQuoteDetails are displayed. If detailFlag is MF_DETAIL, the MutualFund structure gets displayed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteData(
    val dateTime: GregorianCalendar,
    val quoteStatus: QuoteStatus,
    val ahFlag: Boolean,

    @JsonProperty("Product")
    val product: Product,

    @JsonProperty("All")
    val tickerData: TickerData
) {
    val symbol: String?
        get() = product.symbol
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteResponse(
    @JsonProperty("QuoteData")
    val data: List<QuoteData>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TickerDataResponse(
    @JsonProperty("QuoteResponse")
    val response: QuoteResponse
)

/**
 * @param[symbol] The market symbol for the security
 * @param[description] The text description of the security
 * @param[type] The symbol type
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LookupResult(
    val symbol: String?,
    val description: String?,
    val type: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LookupData(
    @JsonProperty("Data")
    val data: List<LookupResult>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LookupDataResponse(
    @JsonProperty("LookupResponse")
    val response: LookupData
)

/**
 * @param[rho] The rho value, represents the rate of change between an option’s value and a 1% change in the interest rate. This doesn’t have much effect on options pricing these days thanks to JPow and the Fed’s infinite printer.
 * @param[vega] The vega value, measures the sensitivity of the price of an option to changes in volatility
 * @param[theta] The theta value, measures the time decay of an option or the dollar amount an option will lose each day due to the passage of time
 * @param[delta] The delta value, measures the sensitivity of an option’s theoretical value to a change in the price of the underlying asset
 * @param[gamma] The gamma value, measures the rate of change in the delta for each one-point increase in the underlying asset
 * @param[iv] The Implied Volatility (IV)
 * @param[currentValue]	The current value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionGreeks(
    val rho: Float?,
    val vega: Float?,
    val theta: Float?,
    val delta: Float?,
    val gamma: Float?,
    val iv: Float?,
    val currentValue: Boolean
)

/**
 * @param[optionCategory] STANDARD, ALL, MINI
 * @param[optionRootSymbol] The root or underlying symbol of the option
 * @param[timeStamp] The timestamp of the option
 * @param[adjustedFlag] Indicator signifying whether option is adjusted
 * @param[displaySymbol] The display symbol
 * @param[optionType] The option type
 * @param[strikePrice] The agreed strike price for the option as stated in the contract
 * @param[symbol] The market symbol for the option
 * @param[bid] The bid
 * @param[ask] The ask
 * @param[bidSize] The bid size
 * @param[askSize] The ask size
 * @param[inTheMoney] The "in the money" value; a put option is "in the money" when the strike price of the put is above the current market price of the stock
 * @param[volume] The option volume
 * @param[openInterest] The open interest value
 * @param[netChange] The net change value
 * @param[lastPrice] The last price
 * @param[quoteDetail] The option quote detail
 * @param[osiKey] The Options Symbology Initiative (OSI) key containing the option root symbol, expiration date, call/put indicator, and strike price
 * @param[greeks] The Greeks for an option
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionDetails(
    val optionCategory: OptionCategory?,
    val optionRootSymbol: String?,
    val timeStamp: Instant?,
    val adjustedFlag: Boolean?,
    val displaySymbol: String?,
    val optionType: OptionType?,
    val strikePrice: Float?,
    val symbol: String?,
    val bid: Float?,
    val ask: Float?,
    val bidSize: Int?,
    val askSize: Int?,
    val inTheMoney: String?,
    val volume: Int?,
    val openInterest: Int?,
    val netChange: Float?,
    val lastPrice: Float?,
    val quoteDetail: String?,
    val osiKey: String?,

    @JsonProperty("OptionGreeks")
    val greeks: OptionGreeks
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionPair(
    @JsonProperty("Call")
    val call: OptionDetails,

    @JsonProperty("Put")
    val put: OptionDetails
)

/**
 * @param[timeStamp] The option chain response timestamp
 * @param[quoteType] The option chain response quote type
 * @param[nearPrice] The near price in the option chain
 * @param[pairs] List of option pairs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionChainResponse(
    val timeStamp: Instant,
    val quoteType: QuoteStatus,
    val nearPrice: Float,

    @JsonProperty("OptionPair")
    val pairs: List<OptionPair>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionChainRoot(
    @JsonProperty("OptionChainResponse")
    val response: OptionChainResponse
)

/**
 * @param[year] The four-digit year the option will expire
 * @param[month] The month (1-12) the option will expire
 * @param[day] The day (1-31) the option will expire
 * @param[expiryType] Expiration type of the option
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionExpirationDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val expiryType: OptionExpirationType
) {
    val date: ZonedDateTime
        get() {
            return ZonedDateTime.of(
                LocalDate.of(year, month, day),
                LocalTime.of(16, 0),
                ZoneId.of("America/New_York")
            )
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionExpireDateResponse(
    @JsonProperty("ExpirationDate")
    val dates: List<OptionExpirationDate>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OptionExpireDateResponseEnvelope(
    @JsonProperty("OptionExpireDateResponse")
    val response: OptionExpireDateResponse
)

interface MarketApi {

    @GET("v1/market/optionchains")
    fun getOptionChains(@QueryMap options: Map<String, String>): Call<OptionChainRoot>

    @GET("v1/market/quote/{symbol}")
    fun getQuote(@Path("symbol") symbol: String): Call<TickerDataResponse>

    @GET("v1/market/lookup/{search}")
    fun lookup(@Path("search") search: String): Call<LookupDataResponse>

    @GET("v1/market/optionexpiredate")
    fun optionExpireDates(
        @Query("symbol") symbol: String,
        @Query("expiryType") expiryType: OptionExpirationType?
    ): Call<OptionExpireDateResponseEnvelope>
}
