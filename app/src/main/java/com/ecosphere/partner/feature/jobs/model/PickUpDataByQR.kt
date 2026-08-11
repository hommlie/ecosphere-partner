package com.ecosphere.partner.feature.jobs.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickUpDataByQR(
    @SerializedName("order_id")
    val orderId: String? = null,

    @SerializedName("vehicle_id")
    val vehicleId: Int? = null,

    @SerializedName("vehicle_number")
    val vehicleNumber: String? = null,

    @SerializedName("apartment_name")
    val apartmentName: String? = null,

    @SerializedName("latitude")
    val latitude: String? = null,

    @SerializedName("longitude")
    val longitude: String? = null,

    @SerializedName("subcategories")
    val subcategories: List<SubCategory>? = null
) : Parcelable

@Parcelize
data class SubCategory(

    @SerializedName("category_id")
    val categoryId: Int? = null,

    @SerializedName("category_name")
    val categoryName: String? = null,

    @SerializedName("subcategory_id")
    val subcategoryId: Int? = null,

    @SerializedName("subcategory_name")
    val subcategoryName: String? = null
) : Parcelable