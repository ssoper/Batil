package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.util.*

class Market(session: Session,
             production: Boolean? = null,
             verbose: Boolean? = null,
             baseUrl: String? = null): Service(session, production, verbose, baseUrl) {

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

    // Retrieves all options chains for the nearest expiry date
    fun optionChains(symbol: String): OptionChainResponse? {
        return optionChains(symbol, expiryDate = null, strike = null, distance = null)
    }

    // Retrieves all options chains for the specified expiry date
    fun optionChains(symbol: String, expiryDate: GregorianCalendar): OptionChainResponse? {
        return optionChains(symbol, expiryDate = expiryDate, strike = null, distance = null)
    }

    // Retrieves all options chains for the specified expiry date, strike and distance from strike
    fun optionChains(symbol: String,
                     expiryDate: GregorianCalendar?,
                     strike: Float?,
                     distance: Int?): OptionChainResponse? {
        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(MarketApi::class.java, module)
        val options = mutableMapOf("symbol" to symbol)

        expiryDate?.let {
            options.putAll(mapOf(
                "expiryYear" to it.get(Calendar.YEAR).toString(),
                "expiryMonth" to it.get(Calendar.MONTH).toString(),
                "expiryDay" to it.get(Calendar.DAY_OF_MONTH).toString()))
        }

        strike?.let {
            val format = DecimalFormat("#.##")
            format.roundingMode = RoundingMode.CEILING
            options.put("strikePriceNear", format.format(strike))
        }

        distance?.let {
            options.put("noOfStrikes", ((distance*2)+1).toString())
        }

        val response = service.getOptionChains(options).execute()

        return response.body()?.response
    }
}