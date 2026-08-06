package com.ecosphere.partner.feature.patients.model

import com.google.gson.annotations.SerializedName
import com.ecosphere.partner.model.Pagination

data class PatientsData(
    @SerializedName("patients")
    val patients: List<Patient>,

    @SerializedName("pagination")
    val pagination: Pagination
)