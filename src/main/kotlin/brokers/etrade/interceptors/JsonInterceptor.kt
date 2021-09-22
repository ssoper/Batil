package com.seansoper.batil.brokers.etrade.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class JsonInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("Accept", "application/json")
            .build()

        return chain.proceed(request)
    }
}
