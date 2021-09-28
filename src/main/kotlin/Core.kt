package com.seansoper.batil

import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Orders
import com.seansoper.batil.brokers.etrade.services.orderPreview.buyIronButterfly
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
                // val request = buyIronCondor("ACB", Pair(5.5f, 6f), Pair(6.5f, 7f), .36f, 10)
                // val request = buyButterflyCalls("CHPT", Triple(18f, 19f, 20f), .19f, 10)
                // val request = sellButterflyCalls("CHPT", Triple(18f, 19f, 20f), .19f, 10)
                // val request = buyButterflyPuts("CHPT", Triple(18f, 19f, 20f), .19f, 10)
                val request = buyIronButterfly("TSP", Triple(30f, 35f, 40f), .2f, 10)

                service.createPreview(accountIdKey, request)?.let {
                    println(it)
                }
            }
        }

        // client.destroySession()
    }
}
