package com.ecosphere.partner.feature.permissions.manager

import android.app.Activity
import android.content.Intent
import com.ecosphere.partner.core.common.Constants
import com.ecosphere.partner.feature.permissions.ui.PermissionsAct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionGuard @Inject constructor(
    private val permissionManager: PermissionManager
) {

    fun shouldShowPermissionScreen(): Boolean {
        return !permissionManager.hasAllRequiredPermissions() ||
                !permissionManager.isGpsEnabled()
    }

    fun check(activity: Activity) {

        if (activity is PermissionsAct) return
        if (!shouldShowPermissionScreen()) return

        activity.startActivity(
            Intent(activity, PermissionsAct::class.java).apply {
                putExtra(Constants.EXTRA_FROM, Constants.FROM_RUNTIME)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }

}