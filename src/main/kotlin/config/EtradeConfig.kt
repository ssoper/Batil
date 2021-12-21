package com.seansoper.batil.config

// TODO: Bifurcate this config like Alpaca client does, have username + password and then live flag

data class EtradeConfig(
    val sandbox: EtradeAuth,
    val production: EtradeAuth,
    val username: String,
    val password: String
) {

    operator fun get(env: String): EtradeAuth {
        return if (env == "production") {
            production
        } else {
            sandbox
        }
    }
}

data class EtradeAuth(
    val key: String,
    val secret: String
)
