package com.ecosphere.partner.model

import com.google.gson.annotations.SerializedName

data class CommonResponse<T>(

    @SerializedName("status")
    val success: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("body")
    val data: T? = null,

)
