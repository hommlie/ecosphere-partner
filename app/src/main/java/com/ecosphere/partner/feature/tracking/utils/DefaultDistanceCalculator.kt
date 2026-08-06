package com.ecosphere.partner.feature.tracking.utils

import android.location.Location
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDistanceCalculator @Inject constructor() :
    DistanceCalculator {

    override fun calculate(
        previous: TrackingPoint?,
        current: TrackingPoint
    ): Float {

        if (previous == null) {
            return 0f
        }

        val result = FloatArray(1)

        Location.distanceBetween(
            previous.latitude,
            previous.longitude,
            current.latitude,
            current.longitude,
            result
        )

        return result[0]
    }
}