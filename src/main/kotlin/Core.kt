package com.seansoper.batil

import com.seansoper.batil.connectors.Etrade
import com.seansoper.batil.connectors.EtradeAuthResponse
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Paths
import java.security.KeyStore
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess
import javax.crypto.KeyGenerator




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
        val requestToken = client.getRequestToken()
        val verifier = client.getVerifierCode(requestToken.accessToken)

        if (parsed.verbose) {
            println("Verifier code is $verifier")
        }

        val oauthToken = client.getAccessToken(requestToken, verifier, true)

        if (parsed.verbose) {
            oauthToken.apply {
                println("Access OAuth token is $accessToken")
                println("Access OAuth secret is $accessSecret")
            }
        }

        client.renewAccessToken(oauthToken)

        // val data = client.ticker("AAPL", oauthToken, verifier)
        // val data = client.lookup("Game", oauthToken, verifier)
        // val data = client.optionChains("AAPL", oauthToken, verifier)
        val data = client.optionChains("AAPL", GregorianCalendar(2021, 2, 5), 131f, 1, oauthToken, verifier)
        data?.let {
            print(it)
        }
    }

}
