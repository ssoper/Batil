package com.seansoper.batil

import com.seansoper.batil.brokers.etrade.Accounts
import com.seansoper.batil.brokers.etrade.Authorization
import com.seansoper.batil.brokers.etrade.Orders
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
                service.list(accountIdKey)?.let {
                    println("Orders for account $accountIdKey")
                    println(it)
                }
            }
        }

        // client.destroySession()
    }
}
