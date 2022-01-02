package com.seansoper.batil.brokers.etrade.auth

import com.seansoper.batil.brokers.etrade.interceptors.OauthKeyProvider

class Session(
    override val consumerKey: String,
    override val consumerSecret: String,
    override var accessToken: String,
    override var accessSecret: String,
    override var verifier: String
) : OauthKeyProvider
