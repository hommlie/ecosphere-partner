package com.ecosphere.partner.feature.tracking.offline.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queued_tracking_points")
data class QueuedTrackingPointEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: String,

    val latitude: Double,

    val longitude: Double,

    val accuracy: Float,

    val speed: Float,

    val bearing: Float,

    val altitude: Double,

    val timestamp: Long
)