package com.seansoper.batil.connectors

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ErrorInterceptor: Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val response = chain.proceed(original)

        if (response.code >= 400) {
            response.body?.string()?.let {
                val xmlMapper = XmlMapper()
                throw xmlMapper.readValue(it, EtradeError::class.java)
            }

            throw EtradeError(response.code, "HTTP Error: ${original.url}")
        }

        return response
    }
}
