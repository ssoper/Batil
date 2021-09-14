package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

open class Service(
    session: Session,
    _production: Boolean?,
    _verbose: Boolean?,
    _baseUrl: String?
) {

    private val production = _production ?: false
    private val verbose = _verbose ?: false
    private val baseUrl = _baseUrl ?: "https://${(if (production) "api" else "apisb")}.etrade.com"

    private val keys = OauthKeys(
        consumerKey = session.consumerKey,
        consumerSecret = session.consumerSecret,
        accessToken = session.accessToken,
        accessSecret = session.accessSecret,
        verifier = session.verifier
    )

    fun <T> createClient(javaClass: Class<T>, module: SimpleModule? = null): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .addInterceptor(JsonInterceptor())
            .addInterceptor(ErrorInterceptor())

        if (verbose) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logger)
        }

        val mapper = ObjectMapper()

        module?.let {
            mapper.registerModule(it)
        }

        mapper.registerModule(KotlinModule())

        val retrofit = Retrofit.Builder()
            .client(client.build())
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        return retrofit.create(javaClass)
    }
}
