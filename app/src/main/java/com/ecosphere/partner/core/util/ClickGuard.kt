package com.ecosphere.partner.core.util

import android.os.SystemClock

object ClickGuard {

    private const val CLICK_INTERVAL = 300L
    private var lastClickTime = 0L

    fun isClickAllowed(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < CLICK_INTERVAL) return false
        lastClickTime = now
        return true
    }
}