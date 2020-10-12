package com.seansoper.batil.connectors

import com.seansoper.batil.Configuration
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception

data class EtradeAuthResponse(val accessToken: String,
                              val accessSecret: String) {

    companion object {
        fun withResponse(response: Response): EtradeAuthResponse {
            val tokens = response.body?.string()?.split("&").takeIf {
                it?.isNotEmpty() ?: false
            }?.map { it.split("=", limit = 2) }?.filter {
                it.size == 2 && it.first().contains("token")
            }?.also {
                it.size == 2
            }?.associate { it[0] to it[1] }

            return tokens?.let {
                val token = it["oauth_token"] ?: throw EtradeAuthResponseError("No token returned")
                val secret = it["oauth_token_secret"] ?: throw EtradeAuthResponseError("No secret returned")
                EtradeAuthResponse(token, secret)
            } ?: throw EtradeAuthResponseError("Could not parse tokens from response")
        }
    }

}

class EtradeAuthResponseError(message: String? = "Error in auth response"): Exception(message)

class Etrade(private val configuration: Configuration,
             private val production: Boolean,
             private val verbose: Boolean = false) {

    private val consumerKey: String
        get() {
            return if (production) {
                configuration.etrade.production.key
            } else {
                configuration.etrade.sandbox.key
            }
        }

    private val consumerSecret: String
        get() {
            return if (production) {
                configuration.etrade.production.secret
            } else {
                configuration.etrade.sandbox.secret
            }
        }

    private val domainPrefix: String
        get() {
            return if (production) {
                "api"
            } else {
                "apisb"
            }
        }

    private val paths: HashMap<String, String> = hashMapOf (
        AUTH_REQUEST_TOKEN to "oauth/request_token",
        AUTH_ACCESS_TOKEN to "oauth/access_token"
    )

    companion object {
        private const val AUTH_REQUEST_TOKEN = "auth_request_token"
        private const val AUTH_ACCESS_TOKEN = "auth_access_token"
    }

    private fun urlForPath(path: String): String {
        return "https://${domainPrefix}.etrade.com/${paths[path]}"
    }

    fun requestToken(): EtradeAuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val request = Request.Builder()
            .url(urlForPath(AUTH_REQUEST_TOKEN))
            .build()

        return EtradeAuthResponse.withResponse(client.newCall(request).execute())
    }

    fun verifierCode(token: String): String {
        val browserAuth = EtradeBrowserAuth(
                consumerKey,
                token,
                configuration.etrade.username,
                configuration.etrade.password,
                configuration.chromium,
                verbose
        )

        return browserAuth.retrieve()
    }
}
