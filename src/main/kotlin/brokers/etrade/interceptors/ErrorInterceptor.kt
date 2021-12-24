package com.seansoper.batil.brokers.etrade.interceptors

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.seansoper.batil.brokers.etrade.services.EtradeServiceError
import com.seansoper.batil.brokers.etrade.services.ExpiredTokenError
import com.seansoper.batil.brokers.etrade.services.ServiceUnavailableError
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
                val error = XmlMapper().readValue(it, EtradeServiceError::class.java)

                if (error.code == 0 && error.message == "oauth_problem=token_expired") {
                    ExpiredTokenError()
                } else if (error.code == 100) {
                    ServiceUnavailableError()
                } else {
                    error
                }
            } ?: EtradeServiceError(response.code, "HTTP Error: ${original.url}")
        }

        return response
    }
}
