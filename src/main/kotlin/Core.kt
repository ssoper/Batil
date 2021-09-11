package com.seansoper.batil

import com.seansoper.batil.connectors.etrade.*
import java.util.*
import kotlin.system.exitProcess


object Core {
    @JvmStatic fun main(args: Array<String>) {
        val cli = CommandLineParser(args)

        if (cli.shouldShowHelp) {
            cli.showHelp()
            exitProcess(0)
        }

        val parsed = try {
            cli.parse()
        } catch (exception: ConfigFileNotFound) {
            println("❌ ${exception.localizedMessage}")
            cli.showHelp()
            exitProcess(1)
        }

        if (parsed.verbose) {
            println("Verbose set to ${parsed.verbose}")
            println("Using ${if (parsed.production) { "production" } else { "sandbox" } }")
            println("Config path set to ${parsed.pathToConfigFile}")
        }

        val configuration = try {
            IngestConfiguration(parsed).parse()
        } catch (exception: ConfigFileInvalid) {
            println(exception)
            exitProcess(1)
        }

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

        val service = Market(session, parsed.production, parsed.verbose)
        // val data = client.ticker("AAPL", oauthToken, verifier)
        // val data = client.lookup("Game", oauthToken, verifier)
        // val data = client.optionChains("AAPL", oauthToken, verifier)
        // modify to use third friday from whatever today is
        val data = service.optionChains("AAPL", GregorianCalendar(2021, 9, 17), 131f, 1)

        data?.let {
            println(it)
        }

        val alerts = Alerts(session, parsed.production, parsed.verbose)
        alerts.list()?.let {
            println("Total Alerts: ${it.totalAlerts}")
            if (it.totalAlerts > 0) {
                println(it.alerts)

                alerts.get(it.alerts.first().id)?.let {
                    println("Details for alert")
                    println(it)
                }
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
