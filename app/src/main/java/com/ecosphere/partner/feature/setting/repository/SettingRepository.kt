package com.ecosphere.partner.feature.setting.repository

import com.ecosphere.partner.core.network.ApiService
import com.ecosphere.partner.core.network.SafeApiCall
import javax.inject.Inject

class SettingRepository @Inject constructor(
    private val safeApiCall: SafeApiCall,
    private val apiService: ApiService
) {
    suspend fun getCms() = safeApiCall {
        apiService.getCms()
    }

}