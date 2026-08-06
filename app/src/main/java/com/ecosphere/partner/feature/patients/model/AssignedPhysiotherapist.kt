package com.ecosphere.partner.feature.patients.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignedPhysiotherapist(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("branch_id")
    val branchId: String?,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role_id")
    val roleId: String,

    @SerializedName("super_admin_id")
    val superAdminId: String,

    @SerializedName("experience")
    val experience: String,

    @SerializedName("specialization")
    val specialization: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable