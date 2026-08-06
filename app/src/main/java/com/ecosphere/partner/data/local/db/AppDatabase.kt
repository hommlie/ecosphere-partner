package com.ecosphere.partner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ecosphere.partner.feature.tracking.offline.dao.QueuedTrackingPointDao
import com.ecosphere.partner.feature.tracking.offline.entity.QueuedTrackingPointEntity

@Database(
    entities = [
        QueuedTrackingPointEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun queuedTrackingPointDao(): QueuedTrackingPointDao

}