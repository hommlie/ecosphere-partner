package com.ecosphere.partner.core.network

import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingInterceptor @Inject constructor() {

    fun create(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (AppConfig.IS_DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
}