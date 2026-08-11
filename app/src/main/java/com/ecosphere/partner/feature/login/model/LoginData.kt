package com.ecosphere.partner.feature.login.model

import com.google.gson.annotations.SerializedName
import com.ecosphere.partner.model.Tokens

data class LoginData(

    @SerializedName("driver")
    val user: User ?= null,

    @SerializedName("vehicle")
    val vehicle: Vehicle ?= null,

    @SerializedName("token")
    val tokens: String? = null
)
data class User(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?=null,

    @SerializedName("profile")
    val profile: String?=null
)
data class Vehicle(
    @SerializedName("id")
    val vehicleId: String?=null,

    @SerializedName("vehicleNumber")
    val vehicleNumber: String?=null,
)
