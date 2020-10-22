package com.seansoper.batil.connectors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.seansoper.batil.Configuration
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class Etrade(private val configuration: Configuration,
             private val production: Boolean,
             private val verbose: Boolean = false) {

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

    private val domainPrefix: String
        get() {
            return if (production) {
                "api"
            } else {
                "apisb"
            }
        }

    private val paths: HashMap<String, String> = hashMapOf (
        AUTH_REQUEST_TOKEN to "oauth/request_token",
        AUTH_ACCESS_TOKEN to "oauth/access_token"
    )

    companion object {
        private const val AUTH_REQUEST_TOKEN = "auth_request_token"
        private const val AUTH_ACCESS_TOKEN = "auth_access_token"
    }

    private fun urlForPath(path: String): String {
        return "https://${domainPrefix}.etrade.com/${paths[path]}"
    }

    fun requestToken(): EtradeAuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val request = Request.Builder()
            .url(urlForPath(AUTH_REQUEST_TOKEN))
            .build()

        return EtradeAuthResponse.withResponse(client.newCall(request).execute())
    }

    fun verifierCode(token: String): String {
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

    fun accessToken(requestToken: EtradeAuthResponse, verifier: String): EtradeAuthResponse {
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

        val request = Request.Builder()
                .url(urlForPath(AUTH_ACCESS_TOKEN))
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

    fun ticker(symbol: String, accessToken: EtradeAuthResponse, verifier: String): TickerDataResponse? {
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

        val retrofit = Retrofit.Builder()
            .client(client.build())
            .baseUrl("https://apisb.etrade.com")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        val service = retrofit.create(Market::class.java)
        val response = service.getQuote(symbol).execute()

        return response.body()
    }
}
