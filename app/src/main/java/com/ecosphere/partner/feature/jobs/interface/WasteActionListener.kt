package com.ecosphere.partner.feature.jobs.`interface`

interface WasteActionListener {

    fun onAddPhoto(
        wastePosition: Int
    )

    fun onRemovePhoto(
        wastePosition: Int,
        photo: CapturePhotoUi.Photo
    )

}