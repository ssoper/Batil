package com.seansoper.batil.clients

import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.brokers.etrade.auth.Session
import com.seansoper.batil.brokers.etrade.services.Accounts
import com.seansoper.batil.brokers.etrade.services.Market
import com.seansoper.batil.config.ClientConfig
import com.seansoper.batil.config.ConfigFileNotFound
import com.seansoper.batil.config.GlobalConfig
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.vararg
import java.io.File
import kotlin.system.exitProcess

object Etrade {

    @JvmStatic fun main(args: Array<String>) {
        val (parsed, command, meta) = parse(args)

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
            Command.GET_BALANCES -> getBalances(meta.first(), session, parsed)
            Command.LOOKUP_SYMBOL -> lookupSymbols(meta, session, parsed)
        }

        exitProcess(0)
    }

    private enum class Command {
        VERIFY,
        LIST_ACCOUNTS,
        GET_BALANCES,
        LOOKUP_SYMBOL
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

    private fun getBalances(accountIdKey: String, session: Session, clientConfig: ClientConfig) {
        val service = Accounts(session, clientConfig.production, clientConfig.verbose)
        service.getBalance(accountIdKey)?.let {
            it.balances.apply {
                println("Account ID Key ($accountIdKey)")
                println("Net cash: $netCash")
                println("Cash balance: $cashBalance")
                println("Margin balance: ${marginBalance ?: "0"}")
                println("Cash buying power: $cashBuyingPower")
                println("Margin buying power: ${marginBuyingPower ?: "0"}")
                println("Cash available for investment: $cashAvailableForInvestment")
                println("Cash available for withdrawal: $cashAvailableForWithdrawal")
            }
        }
    }

    private fun lookupSymbols(symbols: List<String>, session: Session, clientConfig: ClientConfig) {
        val service = Market(session, clientConfig.production, clientConfig.verbose)
        service.tickers(symbols)?.let { tickers ->
            tickers.forEach {
                it.tickerData.apply {
                    println()
                    println(symbolDescription)
                    lastTrade?.let { println("Last bid: $it") }
                    eps?.let { println("Earnings per share: $it") }
                    totalVolume?.let { println("Total volume: $it") }
                }
            }
        }
    }

    @Throws(ConfigFileNotFound::class)
    private fun parse(args: Array<String>): Triple<ClientConfig, Command, List<String>> {
        val parser = ArgParser("Batil")
        val config by parser.option(ArgType.String, description = "Path to YAML configuration file").default("batil.yaml")
        val verbose by parser.option(ArgType.Boolean, description = "Show additional debugging output").default(false)
        val production by parser.option(ArgType.Boolean, description = "Use production endpoints, default is sandbox").default(false)

        class Verify : Subcommand("verify", "Verify E*TRADE credentials") {
            var exists = false

            override fun execute() {
                exists = true
            }
        }

        class ListAccounts : Subcommand("list_accounts", "List associated E*TRADE accounts") {
            var exists = false

            override fun execute() {
                exists = true
            }
        }

        class GetBalances : Subcommand("get_balances", "Get balances for an E*TRADE account") {
            val accountIdKey by argument(ArgType.String, "accountIdKey of an account")
            var exists = false

            override fun execute() {
                exists = true
            }
        }

        class Lookup : Subcommand("lookup", "Lookup current prices for list of tickers") {
            val tickers by argument(ArgType.String, "Ticker symbols separated by a space").vararg()
            var exists = false

            override fun execute() {
                exists = true
            }
        }

        val verify = Verify()
        parser.subcommands(verify)

        val listAccounts = ListAccounts()
        parser.subcommands(listAccounts)

        val getBalances = GetBalances()
        parser.subcommands(getBalances)

        val lookup = Lookup()
        parser.subcommands(lookup)

        parser.parse(args)

        var meta: List<String> = listOf()
        val command: Command = if (verify.exists) {
            Command.VERIFY
        } else if (listAccounts.exists) {
            Command.LIST_ACCOUNTS
        } else if (getBalances.exists) {
            meta = listOf(getBalances.accountIdKey)
            Command.GET_BALANCES
        } else {
            meta = lookup.tickers
            Command.LOOKUP_SYMBOL
        }

        val configFile = File(config)
        if (!configFile.exists()) {
            throw ConfigFileNotFound()
        }

        return Triple(
            ClientConfig(configFile.toPath(), verbose, production),
            command,
            meta
        )
    }
}
