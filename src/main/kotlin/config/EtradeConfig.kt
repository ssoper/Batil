package config

data class EtradeConfig(val sandbox: EtradeAuth,
                        val production: EtradeAuth,
                        val username: String,
                        val password: String) {

    operator fun get(env: String): EtradeAuth {
        return if (env == "production") {
            production
        } else {
            sandbox
        }
    }
}

data class EtradeAuth(val key: String,
                      val secret: String)
