package com.seansoper.batil.brokers.etrade

import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Alerts
import com.seansoper.batil.brokers.etrade.services.ExpiredTokenError
import com.seansoper.batil.brokers.etrade.services.Market
import com.seansoper.batil.brokers.etrade.services.Orders
import java.time.Duration

// TODO: Replace verbose with logger interface

class EtradeClient(
    val key: String,
    val secret: String,
    val username: String,
    val password: String,
    val endpoint: Endpoint,
    val verbose: Boolean = false
) {

    enum class Endpoint(val url: String) {
        SANDBOX("https://apisb.etrade.com"),
        LIVE("https://api.etrade.com")
    }

    private val production = endpoint == Endpoint.LIVE

    private val authorization = Authorization(
        key = key,
        secret = secret,
        username = username,
        password = password,
        production = production,
        verbose = verbose,
        baseUrl = endpoint.url
    )

    private val session: Session = authorization.renewSession() ?: authorization.createSession()

    // TODO: Add onRetriesExceeded and log failure using logger
    // TODO: Update verbose usage to use logger
    private val retryPolicy = dev.failsafe.RetryPolicy.builder<Any>()
        .handle(ExpiredTokenError::class.java)
        .withDelay(Duration.ofSeconds(1))
        .withMaxRetries(1)
        .onRetry {
            authorization.renewSession()?.let {
                if (verbose) {
                    println("Re-authorization of session succeeded")
                }
            } ?: run {
                if (verbose) {
                    println("Re-authorization of session failed")
                }
            }
        }
        .build()

    val accounts = Accounts(session, production = production, verbose = verbose)
    val market = Market(session, production = production, verbose = verbose, retryPolicy = retryPolicy)
    val alerts = Alerts(session, production = production, verbose = verbose)
    val orders = Orders(session, production = production, verbose = verbose)
}
