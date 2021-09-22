package com.seansoper.batil.samples

import com.seansoper.batil.OptionsCalendar
import com.seansoper.batil.brokers.etrade.Accounts
import com.seansoper.batil.brokers.etrade.Alerts
import com.seansoper.batil.brokers.etrade.Authorization
import com.seansoper.batil.brokers.etrade.Market
import com.seansoper.batil.brokers.etrade.Orders
import com.seansoper.batil.brokers.etrade.TransactionSortOrder
import com.seansoper.batil.config.GlobalConfig
import com.seansoper.batil.config.RuntimeConfig
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
}
