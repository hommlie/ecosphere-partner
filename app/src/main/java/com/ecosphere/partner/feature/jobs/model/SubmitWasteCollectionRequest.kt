package com.ecosphere.partner.feature.jobs.model

import com.google.gson.annotations.SerializedName

data class SubmitWasteCollectionRequest(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("vehicle_id")
    val vehicleId: Int,

    @SerializedName("driver_id")
    val driverId: Int,

    @SerializedName("remarks")
    val remarks: String,

    @SerializedName("items")
    val items: List<WasteCollectionItemRequest>
)

data class WasteCollectionItemRequest(
    @SerializedName("category_id")
    val categoryId: Int,

    @SerializedName("subcategory_id")
    val subCategoryId: Int,

    @SerializedName("subcategory_name")
    val subCategoryName: String,

    @SerializedName("total_waste_kg")
    val totalWasteKg: Double
)
