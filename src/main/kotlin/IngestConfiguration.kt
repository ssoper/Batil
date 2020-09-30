package com.seansoper.batil

data class EtradeAuth(val key: String,
                      val secret: String)

data class EtradeConfiguration(val sandbox: EtradeAuth,
                               val production: EtradeAuth,
                               val username: String,
                               val password: String)

data class Configuration(val etrade: EtradeConfiguration) {

}

class IngestConfiguration {

}