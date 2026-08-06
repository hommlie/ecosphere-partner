package com.ecosphere.partner.feature.setting.model

import com.google.gson.annotations.SerializedName

data class CmsPageResponse(
    @field:SerializedName("privacy_policy")
    val privacyPolicy: String? = null,

    @field:SerializedName("refund_policy")
    val refundPolicy: String? = null,

    @field:SerializedName("terms_conditions")
    val termsConditions: String? = null,

    @field:SerializedName("about")
    val about: String? = null,

)

