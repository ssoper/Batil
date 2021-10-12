package com.seansoper.batil.clients

import com.seansoper.batil.brokers.etrade.api.OptionExpirationType
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Market
import com.seansoper.batil.config.ClientConfig
import com.seansoper.batil.config.ConfigFileNotFound
import com.seansoper.batil.config.GlobalConfig
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.io.File

object Etrade {

    @JvmStatic fun main(args: Array<String>) {
        val parsed = parse(args)

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
                val service = Market(session, parsed.production, parsed.verbose)
                service.optionExpirationDates("PLTR", OptionExpirationType.WEEKLY)?.let {
                    println(it)
                }
            }
        }

        // client.destroySession()
    }

    @Throws(ConfigFileNotFound::class)
    private fun parse(args: Array<String>): ClientConfig {
        val parser = ArgParser("Batil")
        val config by parser.option(ArgType.String, description = "Path to YAML configuration file").default("batil.yaml")
        val verbose by parser.option(ArgType.Boolean, description = "Show additional debugging output").default(false)
        val production by parser.option(ArgType.Boolean, description = "Use production endpoints, default is sandbox").default(false)
        parser.parse(args)

        val configFile = File(config)
        if (!configFile.exists()) {
            throw ConfigFileNotFound()
        }

        return ClientConfig(configFile.toPath(), verbose, production)
    }
}
