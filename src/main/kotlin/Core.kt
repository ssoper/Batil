package com.seansoper.batil

import okhttp3.OkHttpClient
import okhttp3.Request
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

        val keys = OauthKeys(
                consumerKey = configuration.etrade.sandbox.key,
                consumerSecret = configuration.etrade.sandbox.secret
            )

        val client = OkHttpClient.Builder()
                .addInterceptor(EtradeInterceptor(keys))
                .build()

        val request = Request.Builder()
                .url("https://apisb.etrade.com/oauth/request_token")
                .build()

        val response = client.newCall(request).execute()

        if (parsed.verbose) {
            println(request.headers)
        }

        val tokens = response.body?.string()?.
            split("&").
            takeIf { it?.isNotEmpty() ?: false }?.
            map { it.split("=", limit = 2) }?.
            filter {
                it.size == 2 && it.first().contains("token")
            }?.
            also {
                it.size == 2
            }?.associate { it[0] to it[1] }

        println(tokens)
    }
}
