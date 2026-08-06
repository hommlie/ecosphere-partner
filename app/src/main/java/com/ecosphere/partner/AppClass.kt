package com.ecosphere.partner

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ecosphere.partner.feature.tracking.recovery.CrashRecoveryManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AppClass : Application(), Configuration.Provider{

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var crashRecoveryManager: CrashRecoveryManager

    companion object {
        var currentActivity : Activity?=null
    }

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("AppClass", "Hilt has been initialized")

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        // Force portrait orientation globally
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                activity.window.statusBarColor = Color.WHITE
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    activity.window.decorView.systemUiVisibility =
//                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                }
            }
            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity == activity) currentActivity = null
            }
        })

        applicationScope.launch {
            try {
                crashRecoveryManager.recoverAfterCrash()
            } catch (e: Exception) {
                Log.e("AppClass", "Crash recovery failed", e)
            }
        }
    }
}