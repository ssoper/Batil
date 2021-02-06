package com.seansoper.batil.connectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.batil.Configuration
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.math.RoundingMode
import java.nio.file.Paths
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

    fun getAccessToken(requestToken: EtradeAuthResponse, verifier: String, saveToken: Boolean = false): EtradeAuthResponse {
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
            EtradeAuthResponse.withResponse(response).apply {
                if (saveToken) {
                    saveToken(this)
                }
            }
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

    private fun saveToken(authResponse: EtradeAuthResponse) {
        // check if directory exists, if not create it
        val dirPath = Paths.get(System.getProperty("user.home"), ".batil")

        if (!dirPath.toFile().exists()) {
            dirPath.toFile().mkdir()
        }

        val keyFilePath = Paths.get(dirPath.toString(), "etrade")
        val keys = "${authResponse.accessToken}|${authResponse.accessSecret}"
//        val bytes = keys.byteInputStream(StandardCharsets.UTF_8)
        val stream = FileOutputStream(keyFilePath.toFile())

        val s2 = ByteArrayOutputStream()
        val o2 = ObjectOutputStream(s2)
        o2.writeObject(authResponse)
        o2.flush()

        stream.write(s2.toByteArray())

        // if key file exists, delete it
        // create new key file with keys
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

    fun ticker(symbol: String, accessToken: EtradeAuthResponse, verifier: String): QuoteData? {
        return tickers(listOf(symbol), accessToken, verifier)?.first()
    }

    fun tickers(symbols: List<String>, accessToken: EtradeAuthResponse, verifier: String): List<QuoteData>? {
        val keys = OauthKeys(
                consumerKey = consumerKey,
                consumerSecret = consumerSecret,
                accessToken = accessToken.accessToken,
                accessSecret = accessToken.accessSecret,
                verifier = verifier
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

        val service = retrofit.create(Market::class.java)
        val response = service.getQuote(symbols.joinToString(",")).execute()

        return response.body()?.response?.data
    }

    fun lookup(search: String, accessToken: EtradeAuthResponse, verifier: String): List<LookupResult>? {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = accessToken.accessToken,
            accessSecret = accessToken.accessSecret,
            verifier = verifier
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

        val service = retrofit.create(Market::class.java)
        val response = service.lookup(search).execute()

        return response.body()?.response?.data
    }

    // Retrieves all options chains for the nearest expiry date
    fun optionChains(symbol: String, accessToken: EtradeAuthResponse, verifier: String): OptionChainResponse? {
        return optionChains(symbol, expiryDate = null, strike = null, distance = null, accessToken = accessToken, verifier = verifier)
    }

    // Retrieves all options chains for the specified expiry date
    fun optionChains(symbol: String, expiryDate: GregorianCalendar, accessToken: EtradeAuthResponse, verifier: String): OptionChainResponse? {
        return optionChains(symbol, expiryDate = expiryDate, strike = null, distance = null, accessToken = accessToken, verifier = verifier)
    }

    // Retrieves all options chains for the specified expiry date, strike and distance from strike
    fun optionChains(symbol: String,
                     expiryDate: GregorianCalendar?,
                     strike: Float?,
                     distance: Int?,
                     accessToken: EtradeAuthResponse,
                     verifier: String): OptionChainResponse? {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = accessToken.accessToken,
            accessSecret = accessToken.accessSecret,
            verifier = verifier
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

        val service = retrofit.create(Market::class.java)

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
