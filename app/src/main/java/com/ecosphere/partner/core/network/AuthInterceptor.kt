package com.ecosphere.partner.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val accessToken = tokenStore.accessToken()

        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest =
            originalRequest.newBuilder()
                .header(
                    "Authorization",
                    "Bearer $accessToken"
                )
                .build()

        return chain.proceed(authenticatedRequest)
    }
}