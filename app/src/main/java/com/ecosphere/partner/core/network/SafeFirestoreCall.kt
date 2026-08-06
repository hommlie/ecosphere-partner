package com.ecosphere.partner.core.network

import javax.inject.Inject

class SafeFirestoreCall @Inject constructor(
    private val errorParser: ErrorParser
) {

    suspend operator fun <T> invoke(
        block: suspend () -> T
    ): ApiResult<T?> {

        return try {
            ApiResult.Success(block())
        } catch (throwable: Throwable) {
            errorParser.parse(throwable)
        }
    }
}