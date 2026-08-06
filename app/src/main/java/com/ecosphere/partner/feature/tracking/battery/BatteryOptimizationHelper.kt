package com.ecosphere.partner.feature.tracking.battery

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryOptimizationHelper @Inject constructor(

    @ApplicationContext
    private val context: Context

) {

    fun isIgnoringBatteryOptimization(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val powerManager =
            context.getSystemService(PowerManager::class.java)

        return powerManager.isIgnoringBatteryOptimizations(
            context.packageName
        )
    }

    fun requestIgnoreBatteryOptimization() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (isIgnoringBatteryOptimization()) {
            return
        }

        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {

            context.startActivity(intent)

        } catch (_: Exception) {

            openBatteryOptimizationSettings()

        }

    }

    fun openBatteryOptimizationSettings() {

        val intent = Intent(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {

            context.startActivity(intent)

        } catch (_: ActivityNotFoundException) {
        }

    }

    fun openAutoStartSettings() {

        val manufacturer =
            Build.MANUFACTURER.lowercase()

        val intent = Intent().apply {

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        }

        when {

            manufacturer.contains("xiaomi") -> {

                intent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )

            }

            manufacturer.contains("oppo") -> {

                intent.setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )

            }

            manufacturer.contains("vivo") -> {

                intent.setClassName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )

            }

            manufacturer.contains("realme") -> {

                intent.setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )

            }

            manufacturer.contains("oneplus") -> {

                intent.setClassName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )

            }

            else -> {

                openBatteryOptimizationSettings()
                return
            }
        }

        try {

            context.startActivity(intent)

        } catch (_: Exception) {

            openBatteryOptimizationSettings()
        }
    }

}