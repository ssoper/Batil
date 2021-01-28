package com.seansoper.batil.connectors

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(val description: String,
                   val code: Int,
                   val type: String)

enum class QuoteStatus {
    REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED, UNKNOWN
}

val defaultDate = GregorianCalendar(1970, 1, 1, 1, 0, 0)

object DateSerializer {
    private const val Format = "HH:mm:ss zzz dd-MM-yyyy"
    val Formatter = SimpleDateFormat(Format)

    class Encode: JsonSerializer<GregorianCalendar>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: GregorianCalendar?, gen: JsonGenerator?, serializers: SerializerProvider?) {
            value?.apply {
                gen?.writeString(Formatter.format(time))
            }
        }
    }

    class Decode: JsonDeserializer<GregorianCalendar>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): GregorianCalendar {
            return p?.text?.let {
                val date = Formatter.parse(it)
                val calendar = GregorianCalendar()
                calendar.time = date

                calendar
            } ?: throw DeserializerException()
        }
    }

    class DeserializerException: JsonProcessingException("Could not parse JSON")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TickerData(
    val adjustedFlag: Boolean?,           // Indicates whether an option has been adjusted due to a corporate action (for example, a dividend or stock split)
    val annualDividend: Float?,           // Cash amount paid per share over the past year
    val ask: Float?,                      // The current ask price for a security
    val askExchange: String?,             // Code for the exchange reporting the ask price
    val askSize: Int?,                    // Number shares or contracts offered by broker or dealer at the ask price
    val askTime: String?,                 // The time of the ask; for example, '15:15:43 PDT 03-21-2018'
    val bid: Float?,                      // Current bid price for a security
    val bidExchange: String?,             // Code for the exchange reporting the bid price
    val bidSize: Int?,                    // Number of shares or contracts offered at the bid price
    val bidTime: String?,                 // Time of the bid; for example '15:15:43 PDT 03-21-2018'
    val changeClose: Float?,              // Dollar change of the last price from the previous close
    val changeClosePercentage: Float?,    // Percentage change of the last price from the previous close
    val companyName: String?,             // Name of the company or mutual fund (shows up to 40 characters)
    val daysToExpiration: Int?,           // Number of days before the option expires
    val dirLast: String?,                 // Direction of movement; that is, whether the current price is higher or lower than the price of the most recent trade
    val dividend: Float?,                 // Cash amount per share of the latest dividend
    val eps: Float?,                      // Earnings per share on rolling basis (stocks only)
    val estEarnings: Float?,              // Projected Earnings per share for the next fiscal year (stocks only)
    val exDividendDate: Int?,             // Date (in Epoch time) on which shareholders were entitled to receive the latest dividend
    val exchgLastTrade: String?,          // Code for the exchange of the last trade
    val fsi: String?,                     // Financial Status Indicator indicates whether a Nasdaq-listed issuer has failed to submit its regulatory filings on timely basis, failed to meet continuing listing standards, and/or filed for bankruptcy. Codes are: D - Deficient, E - Delinquent, Q - Bankrupt, N - Normal, G - Deficient and Bankrupt, H - Deficient and Delinquent, J - Delinquent and Bankrupt, and K - Deficient, Delinquent, and Bankrupt.
    val high: Float?,                     // Highest price at which a security has traded during the current day
    val high52: Float?,                   // Highest price at which a security has traded during the past year (52 weeks). For options, this value is the lifetime high.
    val highAsk: Float?,                  // Highest ask price for the current trading day
    val highBid: Float?,                  // Highest bid price for the current trading day
    val lastTrade: Float?,                // Price of the most recent trade of a security
    val low: Float?,                      // Lowest price at which a security has traded during the current day
    val low52: Float?,                    // Lowest price at which security has traded during the past year (52 weeks). For options, this value is the lifetime low.
    val lowAsk: Float?,                   // Lowest ask price for the current trading day
    val lowBid: Float?,                   // Lowest bid price for the current trading day
    val numberOfTrades: Int?,             // Number of transactions involving buying a security from another entity
    val open: Float?,                     // Price of a security at the current day's market open
    val openInterest: Int?,               // Total number of options or futures contracts that are not closed or delivered on a particular day
    val optionStyle: String?,             // Specifies how the contract treats the expiration date. Possible values are "European" (options can be exercised only on the expiration date) or "American" (options can be exercised any time before they expire).
    val optionUnderlier: String?,         // Symbol for the underlier (options only)
    val optionUnderlierExchange: String?, // Exchange code for option underlier symbol; applicable only for options
    val previousClose: Float?,            // Official price at the close of the previous trading day
    val previousDayVolume: Int?,          // Final volume from the previous market session
    val primaryExchange: String?,         // Exchange code of the primary listing exchange for this instrument
    val symbolDescription: String?,       // Description of the security; for example, the company name or the option description
    val todayClose: Float?,               // Price at the close of the regular trading session for the current day
    val totalVolume: Int?,                // Total number of shares or contracts exchanging hands
    val upc: Int?,                        // Uniform Practice Code identifies specific FINRA advisories detailing unusual circumstances; for example, extremely large dividends, when-issued settlement dates, and worthless securities
    val volume10Day: Int?,                // Ten-day average trading volume for the security
    val cashDeliverable: Float?,          // The cash deliverables in case of multiple deliverables
    val marketCap: Float?,                // The value market capitalization
    val sharesOutstanding: Float?,        // The number of outstanding shares
    val nextEarningDate: String?,         // If requireEarningsDate is true, the next earning date value in mm/dd/yyyy format
    val beta: Float?,                     // A measure of a stock's volatility relative to the primary market index
    val yield: Float?,                    // The dividend yield
    val declaredDividend: Float?,         // The declared dividend
    val dividendPayableDate: Int?,        // The dividend payable date
    val pe: Float?,                       // The option multiplier
    val marketCloseBidSize: Int?,         // The market close bid size
    val marketCloseAskSize: Int?,         // The market close ask size
    val marketCloseVolume: Int?,          // The market close volume
    val week52LowDate: Int?,              // The date at which the price was the lowest in the last 52 weeks; applicable for stocks and mutual funds
    val week52HiDate: Int?,               // The date at which the price was highest in the last 52 weeks; applicable for stocks and mutual funds
    val intrinsicValue: Float?,           // The intrinsic value of the share
    val timePremium: Float?,              // The value of the time premium
    val optionMultiplier: Float?,         // The option multiplier value
    val contractSize: Float?,             // CThe contract size of the option
    val expirationDate: Int?,             // The expiration date of the option
    val optionPreviousBidPrice: Float?,   // The option previous bid price
    val optionPreviousAskPrice: Float?,   // OThe option previous ask price
    val osiKey: String?,                  // The Options Symbology Initiative (OSI) representation of the option symbol
    val timeOfLastTrade: Int?,            // The time when the last trade was placed
    val averageVolume: Int?,              // Average volume value corresponding to the symbol

    // TODO
    // val optionDeliverableList: UNKNOWN // List of mulitple deliverables
    // val ehQuote: UNKNOWN               // QuoteDetails when market is in extended hours; appears only for after-hours market and when detailFlag is ALL or all
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteData(
    val dateTime: GregorianCalendar,
    val quoteStatus: QuoteStatus,
    val ahFlag: Boolean,

    @JsonProperty("All")
    val tickerData: TickerData
)

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class LookupResult(
    val symbol: String?,      // The market symbol for the security
    val description: String?, // The text description of the security
    val type: String?         // The symbol type
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

interface Market {

    @GET("v1/market/quote/{symbol}")
    fun getQuote(@Path("symbol") symbol: String): Call<TickerDataResponse>

    @GET("v1/market/lookup/{search}")
    fun lookup(@Path("search") search: String): Call<LookupDataResponse>
}