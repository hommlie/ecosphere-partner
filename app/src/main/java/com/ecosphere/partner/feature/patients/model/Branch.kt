package com.ecosphere.partner.feature.patients.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Branch(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("branch_email")
    val branchEmail: String? = null,

    @SerializedName("branch_phone")
    val branchPhone: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("state")
    val state: String? = null,

    @SerializedName("slot_duration")
    val slotDuration: Int? = null,

    @SerializedName("buffer")
    val buffer: Int? = null,

    @SerializedName("operating_hours")
    val operatingHours: List<OperatingHour> = emptyList(),

    @SerializedName("breaks")
    val breaks: List<BreakDay> = emptyList(),


    @SerializedName("super_admin_id")
    val superAdminId: String? = null,

    @SerializedName("branch_manager_id")
    val branchManagerId: String? = null,


    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("manager")
    val manager: Manager? = null,

    @SerializedName("working_hours_summary")
    val workingHoursSummary: WorkingHoursSummary? = null,

    @SerializedName("breaks_summary")
    val breaksSummary: List<BreakSummary> = emptyList()

) : Parcelable