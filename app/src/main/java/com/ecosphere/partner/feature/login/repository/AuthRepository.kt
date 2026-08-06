package com.ecosphere.partner.feature.login.repository

import com.ecosphere.partner.core.network.ApiService
import com.ecosphere.partner.core.network.SafeApiCall
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val safeApiCall: SafeApiCall)
{
    suspend fun login(body: HashMap<String, String>) =
        safeApiCall {
            apiService.login(body)
        }
}