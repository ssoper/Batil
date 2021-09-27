package com.seansoper.batil

import brokers.etrade.services.orderPreview.sellCondorPuts
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
                val request = sellCondorPuts("SNAP", Pair(82f, 83f), Pair(84f, 85f), .07f, 10)

                service.createPreview(accountIdKey, request)?.let {
                    println(it)
                }
            }
        }

        // client.destroySession()
    }
}
