package com.seansoper.batil.brokers.etrade.auth

class Session(
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String,
    val accessSecret: String,
    val verifier: String
)