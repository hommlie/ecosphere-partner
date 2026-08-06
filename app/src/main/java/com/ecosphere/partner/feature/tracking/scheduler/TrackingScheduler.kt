package com.ecosphere.partner.feature.tracking.scheduler

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ecosphere.partner.feature.tracking.worker.TrackingWatchdogWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingScheduler @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val workManager by lazy {
        WorkManager.getInstance(context)
    }

    fun startWatchdog() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request =
            PeriodicWorkRequestBuilder<TrackingWatchdogWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        workManager.enqueueUniquePeriodicWork(
            WATCHDOG_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun stopWatchdog() {
        workManager.cancelUniqueWork(WATCHDOG_WORK_NAME)
    }

    fun triggerImmediateCheck() {

        Log.d("WATCHDOG", "Enqueuing OneTime Worker")
        val request = OneTimeWorkRequestBuilder<TrackingWatchdogWorker>().build()

        workManager.enqueue(request)
    }

    companion object {
        private const val WATCHDOG_WORK_NAME = "tracking_watchdog_worker"
    }

}