package com.ecosphere.partner.feature.permissions.model

/**
 * One time UI actions emitted by PermissionsViewModel.
 *
 * These actions are consumed by PermissionsActivity.
 */
sealed interface PermissionAction {

    data class RequestRuntimePermission(
        val permission: PermissionType
    ) : PermissionAction

    data object OpenAppSettings : PermissionAction

    data object OpenBatteryOptimizationSettings : PermissionAction

    data object OpenLocationSettings : PermissionAction

    data object ResumeTracking : PermissionAction

    data object Finish : PermissionAction
}