package com.ecosphere.partner.feature.permissions.model

/**
 * Represents the complete UI state of the Permission screen.
 */
data class PermissionUiState(

    val permissions: List<PermissionCard> = emptyList(),
    val currentPermission: PermissionCard? = null,
    val isCompleted: Boolean = false
)
