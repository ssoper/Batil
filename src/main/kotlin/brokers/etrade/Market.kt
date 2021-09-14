package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.util.Calendar
import java.util.GregorianCalendar

enum class PriceType {
    ATNM, ALL
}

class Market(
    session: Session,
    production: Boolean? = null,
    verbose: Boolean? = null,
    baseUrl: String? = null
) : Service(session, production, verbose, baseUrl) {

    fun ticker(symbol: String): QuoteData? {
        return tickers(listOf(symbol))?.first()
    }

    fun tickers(symbols: List<String>): List<QuoteData>? {
        val module = SimpleModule()
        module.addDeserializer(GregorianCalendar::class.java, DateTimeDeserializer())

        val service = createClient(MarketApi::class.java, module)
        val response = service.getQuote(symbols.joinToString(",")).execute()

        return response.body()?.response?.data
    }

    fun lookup(search: String): List<LookupResult>? {
        val module = SimpleModule()
        module.addDeserializer(GregorianCalendar::class.java, DateTimeDeserializer())

        val service = createClient(MarketApi::class.java, module)
        val response = service.lookup(search).execute()

        return response.body()?.response?.data
    }

    /**
     * Retrieves one or many options chains for specified options
     * @param[symbol] The market symbol for the instrument. Example: AAPL
     * @param[expiryDate] Retrieve the options chain for a specific date.
     * @param[strike] The chains fetched will have strike prices near this value.
     * @param[distance] The amount of strikes on either side to return. Example: Assuming an options chain increments by
     * 5 while retrieving the 130 strike, a distance value of 2 means 120, 125, 130, 135, 140 would all be returned.
     * @param[includeWeekly] Whether to include weekly options, default is false.
     * @param[category] Default is STANDARD.
     * @param[chainType] Default is CALLPUT.
     * @param[priceType] Default is ATNM.
     * @sample com.seansoper.batil.Samples.getOptionsChain
     */
    fun optionChains(
        symbol: String,
        expiryDate: GregorianCalendar? = null,
        strike: Float? = null,
        distance: Int? = null,
        includeWeekly: Boolean? = null,
        category: OptionCategory? = null,
        chainType: OptionType? = null,
        priceType: PriceType? = null
    ): OptionChainResponse? {

        val options = mutableMapOf("symbol" to symbol)

        expiryDate?.let {
            options.putAll(
                mapOf(
                    "expiryYear" to it.get(Calendar.YEAR).toString(),
                    "expiryMonth" to it.get(Calendar.MONTH).toString(),
                    "expiryDay" to it.get(Calendar.DAY_OF_MONTH).toString()
                )
            )
        }

        strike?.let {
            val format = DecimalFormat("#.##")
            format.roundingMode = RoundingMode.CEILING
            options.put("strikePriceNear", format.format(it))
        }

        distance?.let {
            options.put("noOfStrikes", ((it * 2) + 1).toString())
        }

        includeWeekly?.let {
            options.put("includeWeekly", it.toString())
        }

        category?.let {
            options.put("optionCategory", it.toString())
        }

        chainType?.let {
            options.put("chainType", it.toString())
        }

        priceType?.let {
            options.put("priceType", it.toString())
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(MarketApi::class.java, module)
        val response = service.getOptionChains(options).execute()

        return response.body()?.response
    }
}
