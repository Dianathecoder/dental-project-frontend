package com.example.dynalar_frontend_v1.network

import com.example.dynalar_frontend_v1.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()


        sessionManager.fetchAuthToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }


        val response = chain.proceed(requestBuilder.build())


        if (response.code == 401 || response.code == 403) {

            sessionManager.clearSession()

        }

        return response
    }
}