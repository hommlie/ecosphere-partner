package com.ecosphere.partner.core.network

sealed interface ApiResult<out T> {

    val errorMessage: String?
        get() = "Something went wrong."

    data class Success<out T>(
        val data: T?
    ) : ApiResult<T?>

    data class ApiError(
        val code: Int,
        val message: String
    ) : ApiResult<Nothing>{
        override val errorMessage = message
    }

    data class NetworkError(
        val message: String = "No internet connection. Please check your internet and try again."
    ) : ApiResult<Nothing>{
        override val errorMessage = message
    }

    data class TimeoutError(
        val message: String = "Request timed out. Please try again."
    ) : ApiResult<Nothing>{

        override val errorMessage = message
    }

    data class SerializationError(
        val message: String = "Unable to process server response."
    ) : ApiResult<Nothing>{

        override val errorMessage = message
    }

    data class UnknownError(
        val message: String = "Something went wrong. Please try again."
    ) : ApiResult<Nothing>{

        override val errorMessage = message
    }
}