package com.ecosphere.partner.feature.jobs.`interface`

interface PhotoActionListener {
    fun onAddClicked()

    fun onRemoveClicked(
        photo: CapturePhotoUi.Photo
    )
}