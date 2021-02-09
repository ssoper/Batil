package com.seansoper.batil

import com.seansoper.batil.connectors.etrade.Authorization
import com.seansoper.batil.connectors.etrade.Market
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
            println("Config path set to ${parsed.pathToConfigFile}")
        }

        val configuration = try {
            IngestConfiguration(parsed).parse()
        } catch (exception: ConfigFileInvalid) {
            println(exception)
            exitProcess(1)
        }

        val client = Authorization(configuration, parsed.production, parsed.verbose)
        val session = client.renewSession() ?: client.createSession()

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
        val data = service.optionChains("AAPL", GregorianCalendar(2021, 2, 12), 131f, 1)

        data?.let {
            print(it)
        }

    }

}
