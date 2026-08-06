package com.ecosphere.partner.core.datastore

data class UserSession(
    val accessToken: String?,
    val driverId: String?,
    val vehicleNumber : String?,
    val driverName: String?=null,
    val driverProfile : String?=null,
    val isLoggedIn: Boolean
)
