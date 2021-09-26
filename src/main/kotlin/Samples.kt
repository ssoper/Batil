package com.seansoper.batil.samples

import brokers.etrade.services.orderPreview.buyCallSpread
import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Alerts
import com.seansoper.batil.brokers.etrade.services.Market
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.TransactionSortOrder
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyCallOptionMarket
import com.seansoper.batil.brokers.etrade.services.orderPreview.sellCallOptionMarket
import com.seansoper.batil.config.GlobalConfig
import com.seansoper.batil.config.RuntimeConfig
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.GregorianCalendar

/**
 * @suppress
 */
class Market {

    fun getOptionsChain(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Market(session, runtime.production, runtime.verbose)
        val date = OptionsCalendar.nextMonthly()
        val result =
            service.optionChains("AAPL", GregorianCalendar(date.year, date.month.value, date.dayOfMonth), 131f, 1)

        result?.let {
            println(it)
        }
    }
}

/**
 * @suppress
 */
class Alerts {

    fun list(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Alerts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            println("Total Alerts: ${it.totalAlerts}")
            if (it.totalAlerts > 0) {
                println(it.alerts)
            }
        }
    }

    fun get(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Alerts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            if (it.totalAlerts > 0) {
                service.get(it.alerts.first().id)?.let {
                    println("Details for alert")
                    println(it)
                }
            }
        }
    }

    fun deleteSingle(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Alerts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            if (it.totalAlerts > 0) {
                service.delete(it.alerts.first().id)?.let {
                    println("Deleted alert")
                    println(it)
                }
            }
        }
    }
    fun deleteMultiple(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Alerts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            if (it.totalAlerts > 0) {
                service.delete(it.alerts.slice(IntRange(0, 5)).map { it.id })?.let {
                    println("Deleted alerts")
                    println(it)
                }
            }
        }
    }
}

/**
 * @suppress
 */

class Accounts {

    fun list(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Accounts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            println("Accounts retrieved")
            println(it)
        }
    }

    fun viewPortfolio(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Accounts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            it.first().accountIdKey?.let { accountIdKey ->
                service.viewPortfolio(accountIdKey)?.let {
                    println("View portfolio")
                    println(it)
                }
            }
        }
    }

    fun listTransactions(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Accounts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            it.first().accountIdKey?.let { accountIdKey ->
                service.listTransactions(accountIdKey, sortOrder = TransactionSortOrder.DESC, count = 5)?.let {
                    println("List five most recent transactions")
                    println(it)
                }
            }
        }
    }

    fun getTransaction(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Accounts(session, runtime.production, runtime.verbose)

        service.list()?.let {
            it.first().accountIdKey?.let { accountIdKey ->
                service.listTransactions(accountIdKey, sortOrder = TransactionSortOrder.DESC, count = 1)?.let {
                    service.getTransaction(accountIdKey, it.transactions.first().transactionId)?.let {
                        println("Most recent transaction")
                        println(it)
                    }
                }
            }
        }
    }
}

/**
 * @suppress
 */
class Orders {

    fun list(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val accounts = Accounts(session, runtime.production, runtime.verbose)

        accounts.list()?.let {
            it.first().accountIdKey?.let { accountIdKey ->
                val service = Orders(session, runtime.production, runtime.verbose)
                service.list(accountIdKey)?.let {
                    println("Orders for account $accountIdKey")
                    println(it)
                }
            }
        }
    }

    fun buyCallOptionLimit(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Orders(session, runtime.production, runtime.verbose)
        val accountIdKey = "ACCOUNT_ID_KEY"
        val expiry = ZonedDateTime.of(
            LocalDate.of(2021, 10, 15),
            LocalTime.of(16, 0),
            ZoneId.of("America/New_York")
        )

        val request = buyCallOptionMarket(
            symbol = "AAPL",
            limitPrice = 5f,
            stopPrice = 2.5f,
            strikePrice = 150f,
            quantity = 1,
            expiry = expiry
        )
        service.createPreview(accountIdKey, request)?.let {
            println("Preview of Buy to Open order of AAPL--211015C00150000")
            println(it)
        }
    }

    fun sellCallOptionLimit(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Orders(session, runtime.production, runtime.verbose)
        val accountIdKey = "ACCOUNT_ID_KEY"
        val expiry = ZonedDateTime.of(
            LocalDate.of(2021, 10, 15),
            LocalTime.of(16, 0),
            ZoneId.of("America/New_York")
        )

        val request = sellCallOptionMarket(
            symbol = "AAPL",
            limitPrice = 5f,
            stopPrice = 2.5f,
            strikePrice = 150f,
            quantity = 1,
            expiry = expiry
        )
        service.createPreview(accountIdKey, request)?.let {
            println("Preview of Sell to Open order of AAPL--211015C00150000")
            println(it)
        }
    }

    fun buyCallDebitSpread(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Orders(session, runtime.production, runtime.verbose)
        val accountIdKey = "ACCOUNT_ID_KEY"
        val expiry = ZonedDateTime.of(
            LocalDate.of(2021, 10, 15),
            LocalTime.of(16, 0),
            ZoneId.of("America/New_York")
        )

        val request = buyCallSpread(
            symbol = "CLF",
            limitPrice = .32f,
            buyStrike = 21f,
            sellStrike = 22f,
            quantity = 1,
            expiry = expiry
        )

        service.createPreview(accountIdKey, request)?.let {
            println("Preview of Buy to Open call debit spread of CLF")
            println(it)
        }
    }

    fun sellPutCreditSpread(runtime: RuntimeConfig = RuntimeConfig.default()) {
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Orders(session, runtime.production, runtime.verbose)
        val accountIdKey = "ACCOUNT_ID_KEY"
        val expiry = ZonedDateTime.of(
            LocalDate.of(2021, 10, 15),
            LocalTime.of(16, 0),
            ZoneId.of("America/New_York")
        )

        val request = buyCallSpread(
            symbol = "CLF",
            limitPrice = .37f,
            buyStrike = 19f,
            sellStrike = 20f,
            quantity = 1,
            expiry = expiry
        )

        service.createPreview(accountIdKey, request)?.let {
            println("Preview of Sell to Open put credit spread of CLF")
            println(it)
        }
    }
}
