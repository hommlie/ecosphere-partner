package com.ecosphere.partner.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "Something went wrong."
)
