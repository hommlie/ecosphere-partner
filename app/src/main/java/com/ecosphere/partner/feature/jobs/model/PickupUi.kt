package com.ecosphere.partner.feature.jobs.model

import androidx.annotation.ColorRes
import com.ecosphere.partner.R

data class PickupUi(

    val pickupId: String,
    val customerName: String,
    val customerPhone: String,

    val status: PickupStatus,

    val wasteType: WasteType,

    val estimatedWeightKg: Float,

    val distanceKm: Double,

    val pickupTime: Long,

    val pickupAddress: String,

    val mapLink: String? = null,

    val latitude: Double?=null,
    val longitude: Double ?= null
)
enum class WasteType(
    @ColorRes val color: Int,
    @ColorRes val lightColor: Int,
    val displayName: String
) {

    WET(
        R.color.waste_wet,
        R.color.waste_wet_light,
        "Wet Waste"
    ),

    DRY(
        R.color.waste_dry,
        R.color.waste_dry_light,
        "Dry Waste"
    ),

    SANITARY(
        R.color.waste_sanitary,
        R.color.waste_sanitary_light,
        "Sanitary Waste"
    ),

    SPECIAL_CARE(
        R.color.waste_mixed,
        R.color.waste_mixed_light,
        "Special Care"
    ),

    OTHER(
        R.color.waste_bulk,
        R.color.waste_bulk_light,
        "Other"
    )
    ;

    companion object {
        fun fromDisplayName(name: String?): WasteType {
            return entries.firstOrNull {
                it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}
enum class PickupStatus(
    @ColorRes val color: Int,
    val displayName: String
) {

    PENDING(
        R.color.pickup_pending,
        "Pending"
    ),

    STARTED(
        R.color.pickup_started,
        "Started"
    ),

    COMPLETED(
        R.color.pickup_completed,
        "Completed"
    ),

    CANCELLED(
        R.color.pickup_cancelled,
        "Cancelled"
    )
}
