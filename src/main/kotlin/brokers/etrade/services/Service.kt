package com.seansoper.batil.brokers.etrade.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.interceptors.ErrorInterceptor
import com.seansoper.batil.brokers.etrade.interceptors.HttpInterceptor
import com.seansoper.batil.brokers.etrade.interceptors.JsonInterceptor
import com.seansoper.batil.brokers.etrade.interceptors.OauthKeys
import dev.failsafe.ExecutionContext
import dev.failsafe.Failsafe
import dev.failsafe.RetryPolicy
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

// TODO: Remove duplicative arguments like production, verbose and baseUrl since Session should hold all of these
// TODO: Cleanup, remove duplicate arguments from Accounts, Markets, etc.

/**
 * Debating whether to keep this class generic or go full E*TRADE specific, current generic implementation would allow
 * me to plug it into mult clients assuming their auth/session scheme isn't too weird. Going full client specific
 * would let me ditch Failsafe and catch specific E*TRADE errors in execute() and modify session vars with new tokens.
 */

open class Service(
    val session: Session,
    _production: Boolean?,
    _verbose: Boolean?,
    _baseUrl: String?,
    val retryPolicy: RetryPolicy<Any>? = null
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
            .addInterceptor(HttpInterceptor(session))
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

    fun <T> execute(call: Call<T>): Response<T> {
        return retryPolicy?.let {
            Failsafe.with(it).get { ctx: ExecutionContext<Response<T>> ->
                if (ctx.attemptCount > 0) {
                    call.clone().execute()
                } else {
                    call.execute()
                }
            }
        } ?: call.execute()
    }

    companion object {
        fun formatDate(date: GregorianCalendar): String {
            val formatter = SimpleDateFormat("MMddyyyy")
            formatter.calendar = date
            return formatter.format(date.time)
        }
    }
}

// TODO: This belongs in a more globally-named package
open class BrokerServiceError(
    open val code: Int = 0,
    override val message: String = "Error from broker API"
) : Error(message)

open class EtradeServiceError(
    override val code: Int = 0,
    override val message: String = "Error from E*TRADE API"
) : BrokerServiceError(code, message)

class ServiceUnavailableError : EtradeServiceError(100, "The requested service is not currently available")
class ExpiredTokenError : EtradeServiceError(101, "The token is expired")
class InvalidTokenError : EtradeServiceError(102, "The token is invalid")
