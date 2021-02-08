package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.batil.connectors.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class Market(private val session: Session,
             private val production: Boolean = false,
             private val verbose: Boolean = false,
             private val baseUrl: String = "https://${(if (production) "api" else "apisb")}.etrade.com") {

    fun ticker(symbol: String): QuoteData? {
        return tickers(listOf(symbol))?.first()
    }

    fun tickers(symbols: List<String>): List<QuoteData>? {
        val keys = OauthKeys(
            consumerKey = session.consumerKey,
            consumerSecret = session.consumerSecret,
            accessToken = session.accessToken,
            accessSecret = session.accessSecret,
            verifier = session.verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .addInterceptor(JsonInterceptor())

        if (verbose) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logger)
        }

        val module = SimpleModule()
        module.addDeserializer(GregorianCalendar::class.java, DateSerializer.Decode())

        val mapper = ObjectMapper()
        mapper.dateFormat = SimpleDateFormat("HH:mm:ss zzz dd-MM-yyyy")
        mapper.registerModule(module)
        mapper.registerModule(KotlinModule())

        val retrofit = Retrofit.Builder()
            .client(client.build())
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        val service = retrofit.create(MarketApi::class.java)
        val response = service.getQuote(symbols.joinToString(",")).execute()

        return response.body()?.response?.data
    }

    fun lookup(search: String): List<LookupResult>? {
        val keys = OauthKeys(
            consumerKey = session.consumerKey,
            consumerSecret = session.consumerSecret,
            accessToken = session.accessToken,
            accessSecret = session.accessSecret,
            verifier = session.verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .addInterceptor(JsonInterceptor())

        if (verbose) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logger)
        }

        val module = SimpleModule()
        module.addDeserializer(GregorianCalendar::class.java, DateSerializer.Decode())

        val mapper = ObjectMapper()
        mapper.dateFormat = SimpleDateFormat("HH:mm:ss zzz dd-MM-yyyy")
        mapper.registerModule(module)
        mapper.registerModule(KotlinModule())

        val retrofit = Retrofit.Builder()
            .client(client.build())
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        val service = retrofit.create(MarketApi::class.java)
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
        val keys = OauthKeys(
            consumerKey = session.consumerKey,
            consumerSecret = session.consumerSecret,
            accessToken = session.accessToken,
            accessSecret = session.accessSecret,
            verifier = session.verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .addInterceptor(JsonInterceptor())
            .addInterceptor(ErrorInterceptor())

        if (verbose) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logger)
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val mapper = ObjectMapper()
        mapper.registerModule(module)
        mapper.registerModule(KotlinModule())

        val retrofit = Retrofit.Builder()
            .client(client.build())
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        val service = retrofit.create(MarketApi::class.java)

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