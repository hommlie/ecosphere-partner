package com.ecosphere.partner.feature.tracking.offline.mapper

import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.offline.entity.QueuedTrackingPointEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingPointMapper @Inject constructor() {

    fun toEntity(
        sessionId: String,
        point: TrackingPoint
    ): QueuedTrackingPointEntity {

        return QueuedTrackingPointEntity(

            sessionId = sessionId,

            latitude = point.latitude,

            longitude = point.longitude,

            accuracy = point.accuracy,

            speed = point.speed,

            bearing = point.bearing,

            altitude = point.altitude,

            timestamp = point.timestamp
        )
    }

    fun toDomain(
        entity: QueuedTrackingPointEntity
    ): TrackingPoint {

        return TrackingPoint(

            latitude = entity.latitude,

            longitude = entity.longitude,

            accuracy = entity.accuracy,

            speed = entity.speed,

            bearing = entity.bearing,

            altitude = entity.altitude,

            timestamp = entity.timestamp
        )
    }

    fun toDomain(
        entities: List<QueuedTrackingPointEntity>
    ): List<TrackingPoint> {

        return entities.map(::toDomain)

    }

    fun toEntity(
        sessionId: String,
        points: List<TrackingPoint>
    ): List<QueuedTrackingPointEntity> {

        return points.map {

            toEntity(sessionId, it)

        }

    }

}