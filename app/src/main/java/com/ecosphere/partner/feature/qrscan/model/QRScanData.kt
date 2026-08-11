package com.ecosphere.partner.feature.qrscan.model

import com.google.gson.annotations.SerializedName

data class QrPickupData(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("customer")
    val customer: String,

    @SerializedName("generator")
    val generator: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("corp")
    val corp: String,

    @SerializedName("zone")
    val zone: String,

    @SerializedName("ward")
    val ward: String,

    @SerializedName("vendor")
    val vendor: String,

    @SerializedName("vehicle")
    val vehicle: String,

    @SerializedName("driver")
    val driver: String
)