package com.seansoper.batil

import com.seansoper.batil.connectors.Etrade
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

        val client = Etrade(configuration, parsed.production, parsed.verbose)
        val requestToken = client.requestToken()
        val verifier = client.verifierCode(requestToken.accessToken)

        if (parsed.verbose) {
            println("Verifier code is $verifier")


        }

        val oauthToken = client.accessToken(requestToken, verifier)

        if (parsed.verbose) {
            oauthToken.apply {
                println("Access OAuth token is $accessToken")
                println("Access OAuth secret is $accessSecret")
            }
        }

        val data = client.optionsChain("AAPL", oauthToken, verifier)
        data?.let {
            print(it)
        }
    }

}
