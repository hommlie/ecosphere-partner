package com.ecosphere.partner.core.datastore

data class UserSession(
    val accessToken: String?,

    val driverId: String?,
    val driverName: String?=null,
    val driverProfile : String?=null,

    val vehicleId : String?,
    val vehicleNumber : String?,

    val isLoggedIn: Boolean
)
