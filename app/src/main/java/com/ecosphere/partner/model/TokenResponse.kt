package com.ecosphere.partner.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("tokens")
    val tokens: Tokens
)
data class Tokens(

    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("refreshTokenExpiry")
    val refreshTokenExpiry: String

)

data class UploadDataUrl(

    @SerializedName("url")
    val image_url: String,
)
