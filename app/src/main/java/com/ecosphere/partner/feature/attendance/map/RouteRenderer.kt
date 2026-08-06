package com.ecosphere.partner.feature.attendance.map

import android.graphics.Color
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RouteRenderer(
    private val googleMap: GoogleMap
) {
    private var routePolyline: Polyline? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    suspend fun render(
        points: List<TrackingPoint>,
        onRenderCompleted: (() -> Unit)? = null
    ) {

        clear()

        /*val route = withContext(Dispatchers.Default) {
            points
                .distinctBy {
                    Triple(
                        it.latitude,
                        it.longitude,
                        it.timestamp
                    )
                }
        } */
        val route = withContext(Dispatchers.Default) {

            val uniquePoints = points.distinctBy {
                Triple(
                    it.latitude,
                    it.longitude,
                    it.timestamp
                )
            }
            PolylineSimplifier.simplify(
                uniquePoints,
                tolerance = 0.0001
            )
        }

        drawPolyline(route)
        drawMarkers(route)
        moveCamera(route, onRenderCompleted)
    }

    private fun clear() {
        routePolyline?.remove()
        startMarker?.remove()
        endMarker?.remove()
    }

    private fun drawPolyline(
        points: List<TrackingPoint>
    ) {
        if (points.isEmpty())
            return

        val latLngs = points.map {
            LatLng(
                it.latitude,
                it.longitude
            )
        }
        routePolyline =
            googleMap.addPolyline(
                PolylineOptions()
                    .addAll(latLngs)
                    .width(10f)
                    .color(Color.parseColor("#0D9387"))
//                    .geodesic(true)
            )
    }

    private fun drawMarkers(
        points: List<TrackingPoint>
    ) {

        if (points.isEmpty())
            return

        val first = points.first()
        val last = points.last()

        startMarker = googleMap.addMarker(

            MarkerOptions()
                .position(
                    LatLng(
                        first.latitude,
                        first.longitude
                    )
                )
                .title("Start")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                    )
                )
        )
        endMarker = googleMap.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        last.latitude,
                        last.longitude
                    )
                )
                .title("End")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                    )
                )
        )
    }

    private fun moveCamera(
        points: List<TrackingPoint>,
        onRenderCompleted: (() -> Unit)?
    ) {

        if (points.isEmpty()) {
            onRenderCompleted?.invoke()
            return
        }

        val builder = LatLngBounds.Builder()
        points.forEach {
            builder.include(
                LatLng(
                    it.latitude,
                    it.longitude
                )
            )
        }
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                builder.build(),
                150
            ),
            object : GoogleMap.CancelableCallback {

                override fun onFinish() {

                    val maxZoom = 16.5f

                    if (googleMap.cameraPosition.zoom > maxZoom) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.zoomTo(maxZoom),
                            object : GoogleMap.CancelableCallback {

                                override fun onFinish() {
                                    onRenderCompleted?.invoke()
                                }

                                override fun onCancel() {
                                    onRenderCompleted?.invoke()
                                }
                            }
                        )
                    }else{
                        onRenderCompleted?.invoke()
                    }
                }
                override fun onCancel() {
                    onRenderCompleted?.invoke()
                }
            }
        )
    }
}