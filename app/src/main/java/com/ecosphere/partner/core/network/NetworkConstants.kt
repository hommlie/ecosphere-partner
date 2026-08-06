package com.ecosphere.partner.core.network

import java.util.concurrent.TimeUnit

object NetworkConstants {
    // Timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    val TIME_UNIT = TimeUnit.SECONDS

    // Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"

    // Header Values
    const val BEARER = "Bearer"
    const val APPLICATION_JSON = "application/json"

    // HTTP Status Codes
    const val HTTP_UNAUTHORIZED = 401

    const val REFRESH_TOKEN = "refreshToken"
}