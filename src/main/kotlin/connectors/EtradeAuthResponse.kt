package com.seansoper.batil.connectors

import okhttp3.Response
import java.net.URLDecoder

data class EtradeAuthResponse(val accessToken: String,
                              val accessSecret: String) {

    companion object {
        fun withResponse(response: Response): EtradeAuthResponse {
            val body = response.body?.string() ?: throw EtradeAuthResponseError("Empty response")
            val tokens = body.split("&").takeIf {
                it?.isNotEmpty() ?: false
            }?.map { it.split("=", limit = 2) }?.filter {
                it.size == 2 && it.first().contains("token")
            }?.also {
                it.size == 2
            }?.associate { it[0] to it[1] }

            return tokens?.let {
                val token = it["oauth_token"]?.decodeUtf8() ?: throw EtradeAuthResponseError("No token returned", body)
                val secret = it["oauth_token_secret"]?.decodeUtf8() ?: throw EtradeAuthResponseError("No secret returned", body)
                EtradeAuthResponse(token, secret)
            } ?: throw EtradeAuthResponseError("Could not parse tokens from response", body)
        }

        private fun String.decodeUtf8() = URLDecoder.decode(this, "UTF-8").replace("%2B", "+")
    }

}

class EtradeAuthResponseError(message: String? = "Error in auth response", val body: String? = null): Exception(message)
