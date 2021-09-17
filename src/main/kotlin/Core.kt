package com.seansoper.batil

import com.seansoper.batil.brokers.etrade.Alerts
import com.seansoper.batil.brokers.etrade.Authorization
import com.seansoper.batil.brokers.etrade.Market
import com.seansoper.batil.config.GlobalConfig
import java.util.GregorianCalendar

object Core {
    @JvmStatic fun main(args: Array<String>) {
        val parsed = CommandLineParser(args).parse()

        if (parsed.verbose) {
            println("Verbose set to ${parsed.verbose}")
            println("Using ${if (parsed.production) { "production" } else { "sandbox" } }")
            println("Config path set to ${parsed.pathToConfigFile}")
        }

        val configuration = GlobalConfig.parse(parsed)
        val client = Authorization(configuration, parsed.production, parsed.verbose)
        val session = client.renewSession() ?: run {
            if (parsed.verbose) {
                println("No valid keys found, re-authorizing")
            }

            client.createSession()
        }

        if (parsed.verbose) {
            session.apply {
                println("Access OAuth token is $accessToken")
                println("Access OAuth secret is $accessSecret")
                println("Verifier code is $verifier")
            }
        }

        /*
        val accountSrvc = Accounts(session, parsed.production, parsed.verbose)
        accountSrvc.list()?.let {
            println("Account retrieved")
            println(it)

            it.first().accountIdKey?.let { accountIdKey ->

                // View portfolio
                accountSrvc.viewPortfolio(accountIdKey)?.let {
                    println("View portfolio")
                    println(it)
                }

                // Retrieve 5 most recent transactions
                accountSrvc.listTransactions(accountIdKey, null, null, TransactionSortOrder.DESC, null, 5)?.let {
                    println("Sorted")
                    println(it)

                    // Get details for most recent transaction
                    accountSrvc.getTransaction(accountIdKey, it.transactions.first().transactionId)?.let {
                        println("Recent transaction")
                        println(it)
                    }
                }

            }
        }
        */

        // client.destroySession()
    }
}
