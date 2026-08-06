package com.ecosphere.partner.core.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ecosphere.partner.model.ErrorResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ErrorParser @Inject constructor(
    private val gson: Gson
) {

    fun parse(
        throwable: Throwable
    ): ApiResult<Nothing> {

        return when (throwable) {

            is CancellationException ->
                throw throwable

            is UnknownHostException ->
                ApiResult.NetworkError()

            is SocketTimeoutException ->
                ApiResult.TimeoutError()

            is IOException ->
                ApiResult.NetworkError()

            is HttpException -> {

                val message = parseHttpError(throwable)

                ApiResult.ApiError(
                    code = throwable.code(),
                    message = message
                )
            }

            is SerializationException,
            is JsonSyntaxException -> {

                ApiResult.SerializationError()
            }

            else -> {

                ApiResult.UnknownError()
            }
        }
    }

    private fun parseHttpError(
        exception: HttpException
    ): String {

        val fallback = when (exception.code()) {
            400 -> "Invalid request."
            401 -> "Please login again."
            403 -> "You don't have permission."
            404 -> "Requested resource not found."
            408 -> "Request timed out."
            500, 502, 503 -> "Server is temporarily unavailable."
            else -> "Something went wrong."
        }

        return try {

            val json = exception.response()?.errorBody()?.string()

            if (json.isNullOrBlank()) {
                fallback
            } else {
                val error = gson.fromJson(json, ErrorResponse::class.java)

                return error.message
                    .takeIf { it.isNotBlank() }
                    ?: fallback
//                gson.fromJson(json, ErrorResponse::class.java)
//                    ?.message
//                    ?.takeIf { it.isNotBlank() }
//                    ?: fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}