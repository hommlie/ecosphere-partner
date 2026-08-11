package com.ecosphere.partner.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationUtils {

    fun openNavigation(
        context: Context,
        latitude: Double? = null,
        longitude: Double? = null,
        mapLink: String? = null
    ) {

        try {

            // --------------------------------------------------
            // Priority 1 : Latitude & Longitude
            // --------------------------------------------------

            if (latitude != null &&
                longitude != null &&
                latitude in -90.0..90.0 &&
                longitude in -180.0..180.0
            ) {

                openWithCoordinates(
                    context,
                    latitude,
                    longitude
                )

                return
            }

            // --------------------------------------------------
            // Priority 2 : Link
            // --------------------------------------------------

            val link = mapLink?.trim()

            if (link.isNullOrBlank()) {

                Toast.makeText(
                    context,
                    "Location unavailable.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            when {

                // maps.app.goo.gl
                link.contains("maps.app.goo.gl", true) -> {
                    launchIntent(
                        context,
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link)
                        )
                    )
                }

                // Google Maps URL
                link.contains("google.com/maps", true) -> {
                    launchIntent(
                        context,
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link)
                        )
                    )
                }

                // geo:
                link.startsWith("geo:", true) -> {
                    launchIntent(
                        context,
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link)
                        )
                    )
                }

                // Coordinates
                Regex(
                    "^[-\\d.]+\\s*,\\s*[-\\d.]+$"
                ).matches(link) -> {

                    val parts = link.split(",")

                    openWithCoordinates(
                        context,
                        parts[0].trim().toDouble(),
                        parts[1].trim().toDouble()
                    )
                }

                else -> {

                    launchIntent(
                        context,
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link)
                        )
                    )
                }
            }

        } catch (_: Exception) {

            Toast.makeText(
                context,
                "Unable to open location.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openWithCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {

        val destination = "$latitude,$longitude"

        // Google Maps Navigation
        val googleIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "google.navigation:q=$destination&mode=d"
            )
        ).apply {
            `package` = "com.google.android.apps.maps"
        }

        if (googleIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(googleIntent)
            return
        }

        // Any Maps App
        val geoIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "geo:$destination?q=$destination"
            )
        )

        if (geoIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(geoIntent)
            return
        }

        // Browser
        launchIntent(
            context,
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=$destination"
                )
            )
        )
    }

    private fun launchIntent(
        context: Context,
        intent: Intent
    ) {

        if (intent.resolveActivity(context.packageManager) != null) {

            context.startActivity(intent)

        } else {

            Toast.makeText(
                context,
                "No application found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}