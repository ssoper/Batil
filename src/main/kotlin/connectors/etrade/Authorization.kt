package com.seansoper.batil.connectors.etrade

import com.seansoper.batil.CachedToken
import com.seansoper.batil.CachedTokenException
import com.seansoper.batil.Configuration
import com.seansoper.batil.connectors.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.HashMap

class Authorization(private val configuration: Configuration,
                    private val production: Boolean = false,
                    private val verbose: Boolean = false,
                    private val baseUrl: String = "https://${(if (production) "api" else "apisb")}.etrade.com") {

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

    private val paths: HashMap<String, String> = hashMapOf (
        GET_REQUEST_TOKEN to "oauth/request_token",
        GET_ACCESS_TOKEN to "oauth/access_token",
        RENEW_ACCESS_TOKEN to "oauth/renew_access_token"
    )

    private val cachedToken = CachedToken(CachedToken.Provider.ETRADE)

    companion object {
        private const val GET_REQUEST_TOKEN = "get_request_token"
        private const val GET_ACCESS_TOKEN = "get_access_token"
        private const val RENEW_ACCESS_TOKEN = "renew_access_token"
        private const val CACHED_KEY_TOKEN = "token"
        private const val CACHED_KEY_SECRET = "secret"
        private const val CACHED_KEY_CODE = "code"
    }

    fun getRequestToken(): EtradeAuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[GET_REQUEST_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        return EtradeAuthResponse.withResponse(client.newCall(request).execute())
    }

    fun getVerifierCode(token: String): String {
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

    fun getAccessToken(requestToken: EtradeAuthResponse, verifier: String): EtradeAuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret,
            verifier = verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[GET_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()

        return try {
            EtradeAuthResponse.withResponse(response)
        } catch (exception: EtradeAuthResponseError) {

            if (verbose) {
                exception.body?.apply {
                    println("Response from service at ${request.url}")
                    println(this)
                }
            }

            throw exception
        }
    }

    fun getSession(requestToken: EtradeAuthResponse, verifier: String, cacheTokens: Boolean = true): Session {
        return getAccessToken(requestToken, verifier).let {
            if (cacheTokens) {
                cachedToken.setEntry(CACHED_KEY_SECRET, it.accessSecret)
                cachedToken.setEntry(CACHED_KEY_TOKEN, it.accessToken)
                cachedToken.setEntry(CACHED_KEY_CODE, verifier)
            }

            Session(consumerKey, consumerSecret, it.accessToken, it.accessSecret, verifier)
        }
    }

    fun renewAccessToken(requestToken: EtradeAuthResponse): Boolean {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(EtradeInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[RENEW_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()
        return response.code == 200
    }

    fun renewSession(): Session? {
        return try {
            val token = cachedToken.getEntry(CACHED_KEY_TOKEN)
            val secret = cachedToken.getEntry(CACHED_KEY_SECRET)
            val code = cachedToken.getEntry(CACHED_KEY_CODE)

            if (token != null && secret != null && code != null) {
                val requestToken = EtradeAuthResponse(token, secret)

                if (renewAccessToken(requestToken)) {
                    Session(consumerKey, consumerSecret, token, secret, code)
                } else {
                    null
                }
            } else {
                null
            }
        } catch(exception: CachedTokenException) {
            null
        }
    }
}