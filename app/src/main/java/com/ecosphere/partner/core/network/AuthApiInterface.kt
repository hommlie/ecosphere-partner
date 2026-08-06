package com.ecosphere.partner.core.network

import com.ecosphere.partner.model.CommonResponse
import com.ecosphere.partner.model.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiInterface {

    @POST("v1/auth/refresh-token")
    fun refreshToken(
        @Body map: HashMap<String, String>
    ): Call<CommonResponse<TokenResponse>>

}