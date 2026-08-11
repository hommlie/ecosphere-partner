package com.ecosphere.partner.model

import com.google.gson.annotations.SerializedName

data class CommonResponse<T>(

    @SerializedName("status")
    val success: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null,

)
data class SingleResponse(
    @SerializedName("status")
    val success: Int,

    @SerializedName("message")
    val message: String,
)
