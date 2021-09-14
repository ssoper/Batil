package com.seansoper.batil.brokers.etrade

import com.seansoper.batil.CachedToken
import com.seansoper.batil.CachedTokenException
import com.seansoper.batil.config.GlobalConfig
import okhttp3.OkHttpClient
import okhttp3.Request

class Authorization(
    private val configuration: GlobalConfig,
    private val production: Boolean = false,
    private val verbose: Boolean = false,
    private val baseUrl: String = "https://${(if (production) "api" else "apisb")}.etrade.com"
) {

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

    private val paths: HashMap<String, String> = hashMapOf(
        GET_REQUEST_TOKEN to "oauth/request_token",
        GET_ACCESS_TOKEN to "oauth/access_token",
        RENEW_ACCESS_TOKEN to "oauth/renew_access_token",
        REVOKE_ACCESS_TOKEN to "oauth/revoke_access_token"
    )

    private val tokenStore = CachedToken(CachedToken.Provider.ETRADE)

    private val cachedTokens: Triple<String, String, String>?
        get() {
            return try {
                val token = tokenStore.getEntry(CACHED_KEY_TOKEN)
                val secret = tokenStore.getEntry(CACHED_KEY_SECRET)
                val code = tokenStore.getEntry(CACHED_KEY_CODE)

                if (token != null && secret != null && code != null) {
                    Triple(token, secret, code)
                } else {
                    null
                }
            } catch (exception: CachedTokenException) {
                null
            }
        }

    companion object {
        private const val GET_REQUEST_TOKEN = "get_request_token"
        private const val GET_ACCESS_TOKEN = "get_access_token"
        private const val RENEW_ACCESS_TOKEN = "renew_access_token"
        private const val REVOKE_ACCESS_TOKEN = "revoke_access_token"
        private const val CACHED_KEY_TOKEN = "token"
        private const val CACHED_KEY_SECRET = "secret"
        private const val CACHED_KEY_CODE = "code"
    }

    fun getRequestToken(): AuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[GET_REQUEST_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        return AuthResponse.withResponse(client.newCall(request).execute())
    }

    fun getVerifierCode(token: String): String {
        val browserAuth = BrowserAuthentication(
            consumerKey,
            token,
            configuration.etrade.username,
            configuration.etrade.password,
            configuration.chromium,
            verbose
        )

        return browserAuth.retrieve()
    }

    fun getAccessToken(requestToken: AuthResponse, verifier: String): AuthResponse {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret,
            verifier = verifier
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[GET_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()

        return try {
            AuthResponse.withResponse(response)
        } catch (exception: AuthResponseError) {

            if (verbose) {
                exception.body?.apply {
                    println("Response from service at ${request.url}")
                    println(this)
                }
            }

            throw exception
        }
    }

    fun renewAccessToken(requestToken: AuthResponse): Boolean {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[RENEW_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()
        return response.code == 200
    }

    fun revokeAccessToken(requestToken: AuthResponse): Boolean {
        val keys = OauthKeys(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            accessToken = requestToken.accessToken,
            accessSecret = requestToken.accessSecret
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpInterceptor(keys))
            .build()

        val path = "$baseUrl/${paths[REVOKE_ACCESS_TOKEN]}"
        val request = Request.Builder()
            .url(path)
            .build()

        val response = client.newCall(request).execute()
        return response.code == 200
    }

    // Session

    fun createSession(cacheTokens: Boolean = true): Session {
        val requestToken = getRequestToken()
        val verifier = getVerifierCode(requestToken.accessToken)

        return getAccessToken(requestToken, verifier).let {
            if (cacheTokens) {
                tokenStore.setEntry(CACHED_KEY_SECRET, it.accessSecret)
                tokenStore.setEntry(CACHED_KEY_TOKEN, it.accessToken)
                tokenStore.setEntry(CACHED_KEY_CODE, verifier)
            }

            Session(consumerKey, consumerSecret, it.accessToken, it.accessSecret, verifier)
        }
    }

    fun renewSession(): Session? {
        return cachedTokens?.let {
            val requestToken = AuthResponse(it.first, it.second)

            if (renewAccessToken(requestToken)) {
                Session(consumerKey, consumerSecret, it.first, it.second, it.third)
            } else {
                null
            }
        } ?: run {
            null
        }
    }

    fun destroySession() {
        cachedTokens?.let {
            val requestToken = AuthResponse(it.first, it.second)
            revokeAccessToken(requestToken)
            tokenStore.destroy()
        }
    }
}
