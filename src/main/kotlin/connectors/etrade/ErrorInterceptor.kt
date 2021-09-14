package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// TODO: Add support to intercept the 204 calls which are not strictly errors but can indicate issues lack no data

class ErrorInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val response = chain.proceed(original)

        if (response.code >= 400) {
            throw response.body?.string()?.let {
                val error = XmlMapper().readValue(it, ApiError::class.java)

                when (error.code) {
                    100 -> ServiceUnavailableError()
                    else -> error
                }
            } ?: ApiError(response.code, "HTTP Error: ${original.url}")
        }

        return response
    }
}

open class ApiError(val code: Int = 0, override val message: String = "Error from E*TRADE API") : Error(message)

class ServiceUnavailableError : ApiError(100, "The requested service is not currently available")
