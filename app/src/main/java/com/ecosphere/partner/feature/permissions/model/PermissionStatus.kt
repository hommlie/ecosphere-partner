package com.ecosphere.partner.feature.permissions.model


/**
 * Represents the current business state of a permission.
 *
 * NOTE:
 * This is independent from Android framework APIs.
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUIRED
}