package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// TODO: Add support to intercept the 204 calls which are not strictly errors but can indicate issues lack no data

class ErrorInterceptor: Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val response = chain.proceed(original)

        if (response.code >= 400) {
            response.body?.string()?.let {
                val xmlMapper = XmlMapper()
                throw xmlMapper.readValue(it, ApiError::class.java)
            }

            throw ApiError(response.code, "HTTP Error: ${original.url}")
        }

        return response
    }
}

class ApiError(val code: Int = 0, override val message: String = "Error from E*TRADE API"): Error(message)
