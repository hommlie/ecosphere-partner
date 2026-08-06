package com.ecosphere.partner.feature.tracking.observer

interface GpsState {

    data object Enabled : GpsState

    data object Disabled : GpsState
}