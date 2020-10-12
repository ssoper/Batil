package com.seansoper.batil.connectors

import okhttp3.Response

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
