package com.ecosphere.partner.feature.permissions.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Represents a single permission card shown on the Permission screen.
 *
 * This is a UI model.
 */
data class PermissionCard(

    val type: PermissionType,

    @DrawableRes
    val iconRes: Int,

    @StringRes
    val titleRes: Int,

    @StringRes
    val descriptionRes: Int,

    @StringRes
    val noteRes: Int? = null,

    val status: PermissionStatus

)
