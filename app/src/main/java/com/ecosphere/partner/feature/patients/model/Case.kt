package com.ecosphere.partner.feature.patients.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Case(

    @SerializedName("id")
    val id: String,

    @SerializedName("case_title")
    val caseTitle: String,

    @SerializedName("cid")
    val cid: String,

    @SerializedName("patient_id")
    val patientId: String,

    @SerializedName("primary_region")
    val primaryRegion: String,

    @SerializedName("side")
    val side: String,

    @SerializedName("case_type")
    val caseType: String,

    @SerializedName("onset")
    val onset: String,

    @SerializedName("duration")
    val duration: String,

    @SerializedName("assigned_physiotherapist_id")
    val assignedPhysiotherapistId: String?,

    @SerializedName("notes")
    val notes: String,

    @SerializedName("pain_intensity")
    val painIntensity: Int,

    @SerializedName("pain_type")
    val painType: List<String>,

    @SerializedName("pain_pattern")
    val painPattern: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable