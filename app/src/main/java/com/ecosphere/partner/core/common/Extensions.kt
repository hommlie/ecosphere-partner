package com.ecosphere.partner.core.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.ecosphere.partner.R
import com.ecosphere.partner.core.util.CustomTypefaceSpan
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.feature.webactivity.PdfViewerActivity
import java.text.DecimalFormat
import java.util.Locale


fun String.toCapOnlyFirstLetter() : String{
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

fun Context.startSlideActivity(intent: Intent) {
    val options = ActivityOptionsCompat.makeCustomAnimation(
        this,
        R.anim.slide_in_right,
        R.anim.no_animation
    )
    this.startActivity(intent, options.toBundle())
}
fun Context.startSlideTopActivity(intent: Intent) {
    val options = ActivityOptionsCompat.makeCustomAnimation(
        this,
        R.anim.slide_in_top,
        R.anim.no_animation
    )
    this.startActivity(intent, options.toBundle())
}
fun Activity.finishSlideActivity(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.no_animation,
            R.anim.slide_out_right
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
    }
}
fun Activity.finishSlideBottomActivity(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.no_animation,
            R.anim.slide_out_top
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_top)
    }
}

fun TextView.setColoredText(fullText: String, colorMap: Map<String, Int>) {
    val spannable = SpannableString(fullText)

    for ((word, color) in colorMap) {
        var startIndex = fullText.indexOf(word)
        while (startIndex >= 0) {
            val endIndex = startIndex + word.length
            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            startIndex = fullText.indexOf(word, endIndex)
        }
    }
    this.text = spannable
}
fun TextView.setBoldText(fullText: String, wordsToBold: List<String>, fontResId: Int) {
    val spannable = SpannableString(fullText)
    val boldTypeface: Typeface? = ResourcesCompat.getFont(context, fontResId)

    for (word in wordsToBold) {
        var startIndex = fullText.indexOf(word)
        while (startIndex >= 0) {
            val endIndex = startIndex + word.length

            boldTypeface?.let {
                spannable.setSpan(
                    CustomTypefaceSpan(it),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            startIndex = fullText.indexOf(word, endIndex)
        }
    }

    this.text = spannable
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun WebView.loadSmartUrl(context: Context, url: String?) {
    if (url.isNullOrBlank()) {
        Toast.makeText(context, "No report link available", Toast.LENGTH_SHORT).show()
        loadUrl("about:blank")
        return
    }

    val finalUrl = if (url.endsWith(".pdf", ignoreCase = true)) {
        // Use Google Docs viewer for PDFs
        "https://docs.google.com/gview?embedded=true&url=$url"
    } else {
        url
    }

    loadUrl(finalUrl)
}

var WebView.resetHistoryOnNextLoad: Boolean
    get() = (getTag(R.id.webview_reset_history) as? Boolean) ?: false
    set(value) = setTag(R.id.webview_reset_history, value)

// Setup WebView
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupWebView(context: Context, scope: LifecycleCoroutineScope,btnReload: View) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        useWideViewPort = true
        loadWithOverviewMode = true
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
    }

    var retryCount = 0
    val maxRetry = 3

    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            if (view == null) return

            // Default blank detection
            val isBlank = url.isNullOrBlank() || url == "about:blank" || view.contentHeight == 0

            if (isBlank) {
                btnReload.visibility = View.VISIBLE
                ProgressDialogUtil.dismiss()
                return
            }

//                view.evaluateJavascript(
//                    """
//    var meta = document.createElement('meta');
//    meta.name = 'viewport';
//    meta.content = 'width=device-width, initial-scale=1.0';
//    document.getElementsByTagName('head')[0].appendChild(meta);
//
//    var embed = document.querySelector('embed');
//    if(embed){
//        embed.style.width='100%';
//        embed.style.height='100%';
//        embed.style.transform='scale(1)';
//        embed.style.transformOrigin='top left';
//    }
//    """.trimIndent()
//                ) {}



            // Check for "No preview available" text in the page
            view.evaluateJavascript(
                "(function(){return document.body.innerText.includes('No preview available');})()"
            ) { result ->
                val noPreview = result == "true"
                btnReload.visibility = if (noPreview) View.VISIBLE else View.GONE
                ProgressDialogUtil.dismiss()
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val clickedUrl = request?.url.toString()

            return if (clickedUrl.endsWith(".pdf", ignoreCase = true)) {
                btnReload.visibility = View.GONE
                val intent = Intent(context, PdfViewerActivity::class.java)
                intent.putExtra("pdf_url", clickedUrl)
                context.startActivity(intent)
                view?.post {
                    if (view.canGoBack()) view.goBack()
                }
                true
            } else {
                false // normal links open inside WebView
            }
        }
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            view?.loadUrl("about:blank")
            btnReload.visibility = View.VISIBLE
            ProgressDialogUtil.dismiss()
            Toast.makeText(context, "Failed to load page", Toast.LENGTH_SHORT).show()
        }
    }

    webChromeClient = object : WebChromeClient() {
        private var isLoading = false
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress < 100 && !isLoading) {
                isLoading = true
                ProgressDialogUtil.showLoadingProgress(context as Activity, scope)
            }
            if (newProgress == 100) {
                isLoading = false
                ProgressDialogUtil.dismiss()
            }
        }
    }
}

inline fun <reified T : Parcelable> Bundle?.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this?.getParcelable(key)
    }
}
inline fun <reified T : Parcelable> Intent.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}

fun String.toCapWords(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
}

fun Double.toKm(): String {

    val km = this / 1000.0
    val formatter = DecimalFormat("0.#")
    return "${formatter.format(km)} km"
}

fun Float.toKm(): String {

    val km = this / 1000f
    val formatter = DecimalFormat("0.#")
    return "${formatter.format(km)} km"
}

@Suppress("DEPRECATION")
fun Context.getAddressFromLatLng(
    latitude: Double,
    longitude: Double,
    onResult: (String) -> Unit
) {
    val geocoder = Geocoder(this, Locale.getDefault())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        geocoder.getFromLocation(
            latitude,
            longitude,
            1,
            object : Geocoder.GeocodeListener {

                override fun onGeocode(addresses: MutableList<Address>) {
                    val address = addresses.firstOrNull()
                        ?.getAddressLine(0)
                        ?: "Address unavailable"

                    onResult(address)
                }

                override fun onError(errorMessage: String?) {
                    onResult("Address unavailable")
                }
            }
        )

    } else {

        try {
            val address = geocoder
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
                ?: "Address unavailable"

            onResult(address)

        } catch (e: Exception) {
            onResult("Address unavailable")
        }
    }
}

