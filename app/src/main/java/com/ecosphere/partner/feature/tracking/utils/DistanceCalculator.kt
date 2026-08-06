package com.ecosphere.partner.feature.tracking.utils

import com.ecosphere.partner.feature.tracking.model.TrackingPoint

interface DistanceCalculator {

    fun calculate(
        previous: TrackingPoint?,
        current: TrackingPoint
    ): Float
}