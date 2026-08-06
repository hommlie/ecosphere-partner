package com.ecosphere.partner.core.network

import com.ecosphere.partner.model.CommonResponse
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class SafeApiCall @Inject constructor(
    private val errorParser: ErrorParser
) {

    suspend operator fun <T> invoke(
        apiCall: suspend () -> Response<CommonResponse<T>>
    ): ApiResult<T?> {

        return try {

            val response = apiCall()

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body()
                ?: return ApiResult.UnknownError("Empty server response.")

            if (body.success == 0) {
                return ApiResult.ApiError(
                    code = response.code(),
                    message = body.message.ifBlank {
                        "Something went wrong."
                    }
                )
            }

            ApiResult.Success(body.data)

        } catch (throwable: Throwable) {
            errorParser.parse(throwable)
        }
    }
}