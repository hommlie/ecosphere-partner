package com.ecosphere.partner.feature.webactivity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reports(
    @SerializedName("id")
    val id: String,

    @SerializedName("temperature")
    val temperature: String?,

    @SerializedName("blood_pressure")
    val bloodPressure: String? = null,

    @SerializedName("oxygen")
    val oxygen: String? = null,

    @SerializedName("auscultation")
    val auscultation: String? = null,

    @SerializedName("heartrate")
    val heartrate: String?,


    @SerializedName("appointment_id")
    val appointmentId: String?,

    @SerializedName("report_link")
    val reportLink: String?,

    @SerializedName("report_link_html")
    val reportLinkHtml: String?,

    @SerializedName("report_time")
    val reportTime: String?,

    @SerializedName("payment_status")
    val paymentStatus: Boolean,

    @SerializedName("doctor_id")
    val doctorId: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("is_doctor_report")
    val isDoctorReport: Boolean,

    @SerializedName("family_id")
    val familyId: String?,

    @SerializedName("report_user_id")
    val reportUserId: String?,

    @SerializedName("ekg")
    val ekg: String? = null,

    @SerializedName("external_reports")
    val externalReports: String? = null,

    @SerializedName("report_user_first_name")
    val reportUserFirstName: String?,

    @SerializedName("report_user_last_name")
    val reportUserLastName: String?,

    @SerializedName("report_user_profile")
    val reportUserProfile: String?,

    @SerializedName("report_user_height")
    val reportUserHeight: String?,

    @SerializedName("report_user_weight")
    val reportUserWeight: String?,

    @SerializedName("report_user_gender")
    val reportUserGender: String?,

    @SerializedName("report_user_relation")
    val reportUserRelation: String?,

    @SerializedName("report_user_preexisting_medication")
    val reportUserPreexistingMedication: String?,

    @SerializedName("report_user_preexisting_condition")
    val reportUserPreexistingCondition: String?,

    @SerializedName("report_user_emergency_contact_name")
    val reportUserEmergencyContactName: String?,

    @SerializedName("report_user_emergency_contact_number")
    val reportUserEmergencyContactNumber: String?,

    @SerializedName("doctor_first_name")
    val doctorFirstName: String?,

    @SerializedName("doctor_last_name")
    val doctorLastName: String?,

    @SerializedName("doctor_profile")
    val doctorProfile: String?,

    @SerializedName("doctor_gender")
    val doctorGender: String?,

    @SerializedName("doctor_specialist")
    val doctorSpecialist: String?,

    @SerializedName("doctor_phone_no")
    val doctorPhoneNo: String?,


    @SerializedName("otoscope_image")
    val otoscopeImages: String?,

    @SerializedName("prescriptions")
    val prescriptions: String?,


    @SerializedName("healthscibe_generated")
    val healthscibeGenerated: Boolean,

    @SerializedName("healthscibe_status")
    val healthscibeStatus: String?,

    @SerializedName("healthscibe_report_link")
    val healthscibeReportlink: String?,

    @SerializedName("healthscibe_audio_link")
    val healthscibeAudioLink: String?,

    @SerializedName("healthscibe_video_link")
    val healthscibeVideoLink: String?
) : Parcelable