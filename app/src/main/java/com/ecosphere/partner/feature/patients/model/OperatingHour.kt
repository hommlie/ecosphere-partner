package com.ecosphere.partner.feature.patients.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import com.ecosphere.partner.model.Pagination
import kotlinx.parcelize.Parcelize

@Parcelize
data class OperatingHour(
    @SerializedName("day")
    val day: Int,

    @SerializedName("start")
    val start: String,

    @SerializedName("end")
    val end: String
) : Parcelable

@Parcelize
data class BreakDay(

    @SerializedName("day")
    val day: Int?=null,

    @SerializedName("breaks")
    val breaks: List<Break> = emptyList()
) : Parcelable

@Parcelize
data class Break(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("start")
    val start: String? = null,

    @SerializedName("end")
    val end: String? = null
): Parcelable

@Parcelize
data class Slot(

    @SerializedName("id")
    val id: String,

    @SerializedName("branch_id")
    val branchId: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable

@Parcelize
data class Manager(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("branch_id")
    val branchId: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("role_id")
    val roleId: String? = null,

    @SerializedName("super_admin_id")
    val superAdminId: String? = null,

    @SerializedName("experience")
    val experience: String? = null,

    @SerializedName("specialization")
    val specialization: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null

): Parcelable

@Parcelize
data class WorkingHoursSummary(

    @SerializedName("days")
    val days: String? = null,

    @SerializedName("time")
    val time: String? = null

): Parcelable

@Parcelize
data class BreakSummary(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("days")
    val days: String? = null,

    @SerializedName("time")
    val time: String? = null

): Parcelable

@Parcelize
data class BranchesData(
    @SerializedName("branches")
    val branches: List<Branch> = emptyList(),

    @SerializedName("pagination")
    val pagination: Pagination? = null
): Parcelable
