package com.seansoper.batil.clients

import com.seansoper.batil.brokers.etrade.api.OptionExpirationType
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Market
import com.seansoper.batil.config.ClientConfig
import com.seansoper.batil.config.ConfigFileNotFound
import com.seansoper.batil.config.GlobalConfig
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import kotlin.system.exitProcess

object Etrade {

    @JvmStatic fun main(args: Array<String>) {
        val (parsed, command) = parse(args)

        println("Verbose set to ${parsed.verbose}")
        println("Using ${if (parsed.production) { "production" } else { "sandbox" } }")
        println("Config path set to ${parsed.pathToConfigFile}")

        val configuration = GlobalConfig.parse(parsed)
        val client = Authorization(configuration, parsed.production, parsed.verbose)
        val session = client.renewSession() ?: run {
            println("No valid keys found, re-authorizing")
            client.createSession()
        }

        if (parsed.verbose) {
            session.apply {
                println("Access OAuth token is $accessToken")
                println("Access OAuth secret is $accessSecret")
                println("Verifier code is $verifier")
            }
        }

        when (command) {
            Command.VERIFY -> println("Connection to account verified")
            Command.LIST_ACCOUNTS -> listAccounts(session, parsed)
        }

        exitProcess(0)
        // client.destroySession()
    }

    private enum class Command {
        VERIFY,
        LIST_ACCOUNTS
    }

    private fun listAccounts(session: Session, clientConfig: ClientConfig) {
        val service = Accounts(session, clientConfig.production, clientConfig.verbose)

        service.list()?.let { accounts ->
            accounts.forEach { account ->
                account.apply {
                    println()
                    println("Account ID ($accountId)")
                    println("Key: $accountIdKey")
                    println("Type: $accountType")
                    println("Name: ${name ?: "None"}")
                    println("Status: ${status ?: "None"}")
                    println("Description: ${description ?: "None"}")
                }
            }
        }
    }

    private fun listOptionChain(symbol: String, session: Session, clientConfig: ClientConfig) {
        val service = Market(session, clientConfig.production, clientConfig.verbose)
        service.optionExpirationDates("PLTR", OptionExpirationType.WEEKLY)?.let {
            println(it)
        }
    }

    @Throws(ConfigFileNotFound::class)
    private fun parse(args: Array<String>): Pair<ClientConfig, Command> {
        val parser = ArgParser("Batil")
        val config by parser.option(ArgType.String, description = "Path to YAML configuration file").default("batil.yaml")
        val verbose by parser.option(ArgType.Boolean, description = "Show additional debugging output").default(false)
        val production by parser.option(ArgType.Boolean, description = "Use production endpoints, default is sandbox").default(false)
        val command by parser.option(ArgType.Choice<Command>(), description = "Command to run on E*TRADE API").required()
        parser.parse(args)

        val configFile = File(config)
        if (!configFile.exists()) {
            throw ConfigFileNotFound()
        }

        return Pair(
            ClientConfig(configFile.toPath(), verbose, production),
            command
        )
    }
}
