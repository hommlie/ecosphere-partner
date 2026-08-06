package com.ecosphere.partner.feature.patients.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("first_name")
    val firstName: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("profile_photo")
    val profilePhoto: String? = null,

    @SerializedName("mrn")
    val mrn: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("phone_no")
    val phoneNo: String? = null,

    @SerializedName("reg_branch_id")
    val regBranchId: String? = null,

    @SerializedName("assigned_physiotherapist_id")
    val assignedPhysiotherapistId: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("registeredBranch")
    val registeredBranch: Branch? = null,

    @SerializedName("assignedPhysiotherapist")
    val assignedPhysiotherapist: AssignedPhysiotherapist? = null,

    @SerializedName("cases")
    val cases: List<Case>? = emptyList(),

    @SerializedName("age")
    val age: Int? = null
) : Parcelable