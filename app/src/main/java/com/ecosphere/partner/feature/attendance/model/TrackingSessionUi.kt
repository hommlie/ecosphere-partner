package com.ecosphere.partner.feature.attendance.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackingSessionUi(

    val sessionNumber: Int = 1,

    val sessionId: String = "",

    val userId: String = "",

    val startTime: Long = 0L,

    val endTime: Long? = null,

    val totalDistance: Float = 0f,

    val totalPoints: Int = 0,

    val status: String = ""
) : Parcelable

