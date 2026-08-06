package com.ecosphere.partner.core.util

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ecosphere.partner.R
import java.util.Calendar

object CommonMethods {

    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        isCancelable: Boolean = true,
        showNegativeButton: Boolean = true,
        positiveText: String = "Yes",
        negativeText: String = "No",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {

        // Prevent BadTokenException
        if (context is Activity) {
            if (context.isFinishing || context.isDestroyed) return
        }

        val dialog = Dialog(context, R.style.AppCompatAlertDialogStyleBig).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.confirmation_dialog)
            setCancelable(isCancelable)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
        }

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)
        val tvOk = dialog.findViewById<TextView>(R.id.tv_ok)
        val tvCancel = dialog.findViewById<TextView>(R.id.tv_cancel)

        tvTitle.text = title
        tvMessage.text = message

        tvOk.text = positiveText
        tvCancel.text = negativeText

        tvOk.setOnClickListener {
            dialog.dismiss()
            onConfirm.invoke()
        }

        if (showNegativeButton) {
            tvCancel.visibility = View.VISIBLE
            tvCancel.setOnClickListener {
                dialog.dismiss()
                onCancel?.invoke()
            }
        } else {
            tvCancel.visibility = View.GONE
        }

        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun setStatusBarColor(activity: Activity, colorRes: Int, lightStatusBar: Boolean = true) {
        val window = activity.window
        val color = ContextCompat.getColor(activity, colorRes)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= 35) {
            try {
                window.setStatusBarContrastEnforced(false)
                window.statusBarColor = color
            } catch (_: NoSuchMethodError) {
                window.statusBarColor = color
            }
        } else {
            window.statusBarColor = color
        }

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = lightStatusBar
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun setNavigationBarColor(activity: Activity, colorRes: Int, lightStatusBar: Boolean = true) {
        val window = activity.window
        val color = ContextCompat.getColor(activity, colorRes)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= 35) {
            try {
                window.setNavigationBarContrastEnforced(false)
                window.setNavigationBarColor(color)
            } catch (_: NoSuchMethodError) {
                window.navigationBarColor = color
            }
        } else {
            window.navigationBarColor = color
        }

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightNavigationBars = lightStatusBar
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun setSystemBarsColor(
        activity: Activity,
        statusBarColor: Int,
        navBarColor: Int,
        lightStatusIcons: Boolean = true,
        lightNavIcons: Boolean = true
    ) {
        setStatusBarColor(activity, statusBarColor, lightStatusIcons)
        setNavigationBarColor(activity, navBarColor, lightNavIcons)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "☀\uFE0F Good Morning"
            in 12..16 -> "\uD83C\uDF24 Good Afternoon"
            else -> "\uD83C\uDF19 Good Evening"
        }
    }


    fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun openMailApps(context: Context,email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Contact Us")
            putExtra(Intent.EXTRA_TEXT, "Hello Team,")
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send email via"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private val PHONE_REGEX = Regex("[6-9][0-9]{9}\$")
    fun isValidPhoneNumber(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        return PHONE_REGEX.matches(phone.trim())
    }

    fun calculateDuration(
        startTime: Long?,
        endTime: Long?
    ): String {

        Log.d("EndTime : " , endTime.toString())
        Log.d("StartTime : " , startTime.toString())

        if (startTime == null) return "--"

        val end = endTime ?: System.currentTimeMillis()

        if (end <= startTime) return "--"

        val durationMillis = end - startTime

        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis / (1000 * 60)) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes} min"
        }
    }

}