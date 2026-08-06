package com.ecosphere.partner.feature.tracking.offline.repository

import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.offline.model.OfflineBatch

interface OfflineQueueRepository {

    suspend fun enqueue(
        sessionId: String,
        point: TrackingPoint
    )

    suspend fun enqueue(
        sessionId: String,
        points: List<TrackingPoint>
    )

    suspend fun getBatch(
        limit: Int
    ): OfflineBatch

    suspend fun deleteBatch(
        ids: List<Long>
    )

    suspend fun hasPendingData(): Boolean

    suspend fun count(): Int

    suspend fun clear()
}