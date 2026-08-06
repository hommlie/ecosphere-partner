package com.ecosphere.partner.feature.permissions.model

/**
 * Represents all permissions required by the application.
 *
 * This enum contains only business identifiers.
 * UI resources (title, icon, description) should not live here.
 */

enum class PermissionType {
    LOCATION,
    BACKGROUND_LOCATION,
    NOTIFICATION,
    BATTERY_OPTIMIZATION,
    GPS
}