package com.seansoper.batil

import brokers.etrade.services.orderPreview.buyCallSpread
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.config.GlobalConfig

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

        val accounts = Accounts(session, parsed.production, parsed.verbose)

        accounts.list()?.let {
            it.first().accountIdKey?.let { accountIdKey ->
                val service = Orders(session, parsed.production, parsed.verbose)

                //  val request = sellCallOptionLimit("GSAT", .05f, 0.5f, 1)
                //  val request = buyCallOptionMarket("AAPL", 5f, stopPrice = 2.5f, 150f, 1)
                //  val request = buyPutOptionLimit("AMC", 5f, 35f, 1)
                //  val request = sellPutOptionLimit("T", .65f, 27f, 1)
                // val request = sellPutOptionMarket("T", .65f, 0f, 27f, 1)
                // val request = buyPutOptionMarket("T", .65f, 0f, 27f, 1)
                // val request = buyEquityMarket("RIOT", 27f, 0f, 50)

                // still need to get data for this
                // val request = sellEquityLimit("RIOT", 27f, 200)

                val request = buyCallSpread("CLF", .32f, 21f, 22f, 1)

                service.createPreview(accountIdKey, request)?.let {
                    println(it)
                }

//                service.list(accountIdKey)?.let {
//                    println("Orders for account $accountIdKey")
//                    println(it)
//                }
            }
        }

        // client.destroySession()
    }
}
