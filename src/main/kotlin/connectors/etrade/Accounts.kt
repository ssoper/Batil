package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Instant

class Accounts(private val session: Session,
               private val production: Boolean = false,
               private val verbose: Boolean = false,
               private val baseUrl: String = "https://${(if (production) "api" else "apisb")}.etrade.com") {

    fun list(): List<Account>? {
        val keys = OauthKeys(
            consumerKey = session.consumerKey,
            consumerSecret = session.consumerSecret,
            accessToken = session.accessToken,
            accessSecret = session.accessSecret,
            verifier = session.verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .addInterceptor(JsonInterceptor())

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

        val service = retrofit.create(AccountsApi::class.java)
        val response = service.getAccounts().execute()

        return response.body()?.response?.accountRoot?.accounts
    }

}
