package com.ecosphere.partner.core.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.exifinterface.media.ExifInterface
import com.airbnb.lottie.LottieAnimationView
import com.ecosphere.partner.R
import java.io.File
import java.io.FileOutputStream
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

    fun showSuccessDialog(context: Context
                          , msg: String
                          ,onAnimationEnd: (() -> Unit)? = null)
            : Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dlg_success)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.findViewById<TextView>(R.id.tv_message)?.text = msg

        val lottie = dialog.findViewById<LottieAnimationView>(R.id.lottieSuccess)
        lottie?.playAnimation()

        lottie?.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (dialog.isShowing) dialog.dismiss()
                onAnimationEnd?.invoke()
            }
        })

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val maxWidth = context.resources.getDimensionPixelSize(R.dimen.dialog_max_width)

        val finalWidth = if (screenWidth > maxWidth) maxWidth else ViewGroup.LayoutParams.MATCH_PARENT

        dialog.window?.setLayout(finalWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)

        dialog.show()
        return dialog
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

    fun openDialPad(context: Context, phoneNum: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNum")
        context.startActivity(intent)
    }


    fun compressImageFromUri(
        context: Context,
        uri: Uri,
        quality: Int = 90,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080,
        usePng: Boolean = false,
        preserveDetails: Boolean = false // NEW → true for OCR/medical documents
    ): Uri? {
        return try {
            // Decode bitmap safely
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return null

            // Read rotation
            val rotationDegrees = context.contentResolver.openInputStream(uri)?.use {
                val exif = ExifInterface(it)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                exifOrientationToDegrees(orientation)
            } ?: 0

            // Rotate if needed
            val rotatedBitmap = if (rotationDegrees != 0) {
                rotateBitmap(originalBitmap, rotationDegrees)
            } else originalBitmap

            // Adjust compression if preserving details
            val (targetQuality, targetWidth, targetHeight) = if (preserveDetails) {
                Triple(95, 1600, 1600) // higher clarity for OCR
            } else {
                Triple(quality, maxWidth, maxHeight)
            }

            // Resize only if needed
            val resizedBitmap = if (
                rotatedBitmap.width > targetWidth || rotatedBitmap.height > targetHeight
            ) {
                getResizedBitmap(rotatedBitmap, targetWidth, targetHeight)
            } else rotatedBitmap

            // Save compressed image
            val ext = if (usePng) "png" else "jpg"
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.$ext")
            FileOutputStream(file).use { out ->
                val format = if (usePng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                resizedBitmap.compress(format, targetQuality, out)
            }

            // Cleanup memory
            if (rotatedBitmap != originalBitmap) originalBitmap.recycle()
            if (resizedBitmap != rotatedBitmap) rotatedBitmap.recycle()

            // Return file URI
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun exifOrientationToDegrees(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    fun getResizedBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = when {
            width > height -> {
                val ratio = width.toFloat() / maxWidth
                Bitmap.createScaledBitmap(bitmap, maxWidth, (height / ratio).toInt(), true)
            }
            height > width -> {
                val ratio = height.toFloat() / maxHeight
                Bitmap.createScaledBitmap(bitmap, (width / ratio).toInt(), maxHeight, true)
            }
            else -> {
                Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)
            }
        }
        return ratioBitmap
    }
}