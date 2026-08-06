package com.ecosphere.partner.feature.tracking.offline.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ecosphere.partner.feature.tracking.offline.entity.QueuedTrackingPointEntity

@Dao
interface QueuedTrackingPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        point: QueuedTrackingPointEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        points: List<QueuedTrackingPointEntity>
    )

    @Query(
        """
        SELECT *
        FROM queued_tracking_points
        ORDER BY timestamp ASC
        LIMIT :limit
        """
    )
    suspend fun getBatch(
        limit: Int
    ): List<QueuedTrackingPointEntity>

    @Query(
        """
        SELECT *
        FROM queued_tracking_points
        WHERE sessionId = :sessionId
        ORDER BY timestamp ASC
        LIMIT :limit
        """
    )
    suspend fun getBatchBySession(
        sessionId: String,
        limit: Int
    ): List<QueuedTrackingPointEntity>

    @Query(
        """
        DELETE
        FROM queued_tracking_points
        WHERE id IN (:ids)
        """
    )
    suspend fun deleteBatch(
        ids: List<Long>
    )

    @Query(
        """
        DELETE
        FROM queued_tracking_points
        """
    )
    suspend fun clear()

    @Query(
        """
        SELECT COUNT(*)
        FROM queued_tracking_points
        """
    )
    suspend fun count(): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM queued_tracking_points
        )
        """
    )
    suspend fun hasPendingData(): Boolean
}