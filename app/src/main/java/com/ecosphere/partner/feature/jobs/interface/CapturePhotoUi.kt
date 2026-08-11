package com.ecosphere.partner.feature.jobs.`interface`

import android.net.Uri

sealed interface CapturePhotoUi {

    data object AddPhoto : CapturePhotoUi

    data class Photo(
        val uri: Uri
    ) : CapturePhotoUi
}