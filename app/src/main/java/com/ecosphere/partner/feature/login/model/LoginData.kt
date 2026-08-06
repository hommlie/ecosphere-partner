package com.ecosphere.partner.feature.login.model

import com.google.gson.annotations.SerializedName
import com.ecosphere.partner.model.Tokens

data class LoginData(

    @SerializedName("driver")
    val user: User?,

    @SerializedName("token")
    val tokens: String?
)
data class User(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?=null,

    @SerializedName("vehicleNumber")
    val vehicleNumber: String?=null,

    @SerializedName("profile")
    val profile: String?=null
)
