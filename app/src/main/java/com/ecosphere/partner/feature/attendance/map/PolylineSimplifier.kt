package com.ecosphere.partner.feature.attendance.map

import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import kotlin.math.abs
import kotlin.math.sqrt

object PolylineSimplifier {

    fun simplify(
        points: List<TrackingPoint>,
        tolerance: Double
    ): List<TrackingPoint> {

        if (points.size < 3)
            return points

        val keep = BooleanArray(points.size)

        keep[0] = true
        keep[points.lastIndex] = true

        simplifySection(
            points,
            0,
            points.lastIndex,
            tolerance,
            keep
        )

        return buildList {
            points.forEachIndexed { index, point ->
                if (keep[index]) {
                    add(point)
                }
            }
        }
    }

    private fun simplifySection(

        points: List<TrackingPoint>,
        start: Int,
        end: Int,
        tolerance: Double,
        keep: BooleanArray
    ) {

        if (end <= start + 1)
            return

        var maxDistance = 0.0
        var index = -1
        val startPoint = points[start]
        val endPoint = points[end]

        for (i in start + 1 until end) {

            val distance = perpendicularDistance(
                points[i],
                startPoint,
                endPoint
            )
            if (distance > maxDistance) {
                maxDistance = distance
                index = i
            }
        }
        if (maxDistance > tolerance) {
            keep[index] = true
            simplifySection(
                points,
                start,
                index,
                tolerance,
                keep
            )
            simplifySection(
                points,
                index,
                end,
                tolerance,
                keep
            )
        }
    }

    private fun perpendicularDistance(
        point: TrackingPoint,
        start: TrackingPoint,
        end: TrackingPoint
    ): Double {
        val x = point.longitude
        val y = point.latitude
        val x1 = start.longitude
        val y1 = start.latitude
        val x2 = end.longitude
        val y2 = end.latitude
        if (x1 == x2 && y1 == y2) {

            return sqrt(
                (x - x1) * (x - x1) +
                        (y - y1) * (y - y1)
            )
        }
        val numerator = abs(
            (y2 - y1) * x -
                    (x2 - x1) * y +
                    x2 * y1 -
                    y2 * x1
        )
        val denominator = sqrt(
            (y2 - y1) * (y2 - y1) +
                    (x2 - x1) * (x2 - x1)
        )
        return numerator / denominator
    }
}