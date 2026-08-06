package com.ecosphere.partner.feature.tracking.offline.repository

import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.offline.dao.QueuedTrackingPointDao
import com.ecosphere.partner.feature.tracking.offline.mapper.TrackingPointMapper
import com.ecosphere.partner.feature.tracking.offline.model.OfflineBatch
import com.ecosphere.partner.feature.tracking.offline.model.QueuedTrackingPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultOfflineQueueRepository @Inject constructor(

    private val dao: QueuedTrackingPointDao,

    private val mapper: TrackingPointMapper

) : OfflineQueueRepository {

    override suspend fun enqueue(
        sessionId: String,
        point: TrackingPoint
    ) {

        dao.insert(
            mapper.toEntity(sessionId, point)
        )

    }

    override suspend fun enqueue(
        sessionId: String,
        points: List<TrackingPoint>
    ) {

        dao.insertAll(
            mapper.toEntity(sessionId, points)
        )

    }

    override suspend fun getBatch(
        limit: Int
    ): OfflineBatch {

        val entities = dao.getBatch(limit)

        return OfflineBatch(

            items = entities.map { entity ->

                QueuedTrackingPoint(

                    id = entity.id,

                    point = mapper.toDomain(entity)

                )

            }

        )
    }

    override suspend fun deleteBatch(
        ids: List<Long>
    ) {

        dao.deleteBatch(ids)

    }

    override suspend fun hasPendingData(): Boolean {

        return dao.hasPendingData()

    }

    override suspend fun count(): Int {

        return dao.count()

    }

    override suspend fun clear() {

        dao.clear()

    }

}