package com.seansoper.batil

import com.seansoper.batil.connectors.etrade.Accounts
import com.seansoper.batil.connectors.etrade.Authorization
import com.seansoper.batil.connectors.etrade.Market
import com.seansoper.batil.connectors.etrade.TransactionSortOrder
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
        val data = service.optionChains("AAPL", GregorianCalendar(2021, 9, 10), 131f, 1)

        data?.let {
            print(it)
        }

        val accountSrvc = Accounts(session, parsed.production, parsed.verbose)
        accountSrvc.list()?.let {
            print("Account retrieved")
            print(it)

            it.first().accountIdKey?.let {

                // Last 50 transactions
//                accountSrvc.listTransactions(it)?.let {
//                    print("Transactions")
//                    print(it)
//                }

                // Transactions between 10/1/2020 and 10/3/2020
//                val startDate = GregorianCalendar(2020, 9, 1)
//                val endDate = GregorianCalendar(2020, 9, 3)
//                accountSrvc.listTransactions(it, startDate, endDate)?.let {
//                    print("Transactions")
//                    print(it)
//                }

                // Sort
                accountSrvc.listTransactions(it, null, null, TransactionSortOrder.DESC, null, 5)?.let {
                    print("Sorted")
                    print(it)
                }

            //                accountSrvc.getBalance(it)?.let {
//                    print("Account balance")
//                    print(it)
//                }
            }
        }

        // client.destroySession()
    }

}
