package com.seansoper.batil.connectors

class EtradeError(val code: Int = 0, override val message: String = "Error from E*TRADE API"): Error(message)
