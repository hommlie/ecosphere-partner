package com.ecosphere.partner.feature.jobs.model

import android.net.Uri

data class WasteCollectionUi(
    val categoryId: Int,
    val categoryName: String,
    val subCategoryId: Int,
    val wasteType : WasteType,
    var weight: String = "",
    val images: MutableList<Uri> = mutableListOf()
)
