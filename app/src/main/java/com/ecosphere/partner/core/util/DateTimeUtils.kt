package com.ecosphere.partner.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val formatter = SimpleDateFormat(
        "dd MMM yyyy hh:mm:ss a",
        Locale.getDefault()
    )

    fun format(epochMillis: Long): String {
        return formatter.format(Date(epochMillis))
    }

    fun Long.toTime12Hour(): String {
        return try {
            SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            )
                .format(Date(this))
                .uppercase(Locale.ENGLISH)
        } catch (e: Exception) {
            "--"
        }
    }
}