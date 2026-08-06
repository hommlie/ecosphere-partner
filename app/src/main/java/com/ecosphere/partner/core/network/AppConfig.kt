package com.ecosphere.partner.core.network

object AppConfig {

    private enum class Environment {
        DEV,
        PROD
    }

    // Bas is line ko change karna hoga
    private val CURRENT_ENV = Environment.DEV

    private data class Config(
        val baseUrl: String,
        val appName: String
    )

    private val config = when (CURRENT_ENV) {

        Environment.DEV -> Config(
            baseUrl = "https://ecosphere-gsdg.onrender.com/api/",
            appName = "Ecosphere Partner Dev"
        )

        Environment.PROD -> Config(
            baseUrl = "https://physio.octor-connect.com/api/",
            appName = "Ecosphere Partner"
        )
    }

    val BASE_URL
        get() = config.baseUrl

    val APP_NAME
        get() = config.appName

    val IS_PRODUCTION
        get() = CURRENT_ENV == Environment.PROD

    val IS_DEBUG
        get() = !IS_PRODUCTION
}