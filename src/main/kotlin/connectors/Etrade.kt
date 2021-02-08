package com.seansoper.batil.connectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.batil.Configuration
import com.seansoper.batil.connectors.etrade.Session
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class Etrade(private val configuration: Configuration,
             private val production: Boolean = false,
             private val verbose: Boolean = false,
             private val baseUrl: String = "https://${(if (production) "api" else "apisb")}.etrade.com") {

    private val consumerKey: String
        get() {
            return if (production) {
                configuration.etrade.production.key
            } else {
                configuration.etrade.sandbox.key
            }
        }

    private val consumerSecret: String
        get() {
            return if (production) {
                configuration.etrade.production.secret
            } else {
                configuration.etrade.sandbox.secret
            }
        }

    private val paths: HashMap<String, String> = hashMapOf (
        GET_REQUEST_TOKEN to "oauth/request_token",
        GET_ACCESS_TOKEN to "oauth/access_token",
        RENEW_ACCESS_TOKEN to "oauth/renew_access_token"
    )

    companion object {
        private const val GET_REQUEST_TOKEN = "get_request_token"
        private const val GET_ACCESS_TOKEN = "get_access_token"
        private const val RENEW_ACCESS_TOKEN = "renew_access_token"
    }

    fun getRequestToken(): EtradeAuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[GET_REQUEST_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        return EtradeAuthResponse.withResponse(client.newCall(request).execute())
    }

    fun getVerifierCode(token: String): String {
        val browserAuth = EtradeBrowserAuth(
                consumerKey,
                token,
                configuration.etrade.username,
                configuration.etrade.password,
                configuration.chromium,
                verbose
        )

        return browserAuth.retrieve()
    }

    fun getAccessToken(requestToken: EtradeAuthResponse, verifier: String, useCachedToken: Boolean = true): EtradeAuthResponse {
        val keys = OauthKeys(
                consumerKey = consumerKey,
                consumerSecret = consumerSecret,
                accessToken = requestToken.accessToken,
                accessSecret = requestToken.accessSecret,
                verifier = verifier
        )

        val client = OkHttpClient.Builder()
                .addInterceptor(EtradeInterceptor(keys))
                .build()

        val path = "$baseUrl/${paths[GET_ACCESS_TOKEN]}"
        val request = Request.Builder()
                .url(path)
                .build()

        val response = client.newCall(request).execute()

        return try {
            EtradeAuthResponse.withResponse(response)
        } catch (exception: EtradeAuthResponseError) {

            if (verbose) {
                exception.body?.apply {
                    println("Response from service at ${request.url}")
                    println(this)
                }
            }

            throw exception
        }
    }

    fun renewAccessToken(requestToken: EtradeAuthResponse): Boolean {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[RENEW_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()
        return response.code == 200
    }

    fun ticker(symbol: String, session: Session): QuoteData? {
        return tickers(listOf(symbol), session)?.first()
    }

    fun tickers(symbols: List<String>, session: Session): List<QuoteData>? {
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

    fun lookup(search: String, session: Session): List<LookupResult>? {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
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
    fun optionChains(symbol: String, session: Session): OptionChainResponse? {
        return optionChains(symbol, expiryDate = null, strike = null, distance = null, session = session)
    }

    // Retrieves all options chains for the specified expiry date
    fun optionChains(symbol: String, expiryDate: GregorianCalendar, session: Session): OptionChainResponse? {
        return optionChains(symbol, expiryDate = expiryDate, strike = null, distance = null, session = session)
    }

    // Retrieves all options chains for the specified expiry date, strike and distance from strike
    fun optionChains(symbol: String,
                     expiryDate: GregorianCalendar?,
                     strike: Float?,
                     distance: Int?,
                     session: Session): OptionChainResponse? {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
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
