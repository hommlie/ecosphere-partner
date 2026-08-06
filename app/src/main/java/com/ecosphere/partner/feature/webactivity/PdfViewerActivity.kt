package com.ecosphere.partner.feature.webactivity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecosphere.partner.core.common.loadSmartUrl
import com.ecosphere.partner.core.common.setupWebView
import com.ecosphere.partner.databinding.ActivityPdfViewerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PdfViewerActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfViewerBinding

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = WebView(this)
        binding.webViewContainer.addView(webView)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.setupWebView(this, lifecycleScope,binding.btnReload)

        val pdfUrl = intent.getStringExtra("pdf_url")
        webView.loadSmartUrl(this, pdfUrl)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        } else {
            // For older versions, keep onBackPressed()
            @Suppress("DEPRECATION")
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        this@PdfViewerActivity.finish()
                    }
                }
            })
        }

        binding.btnReload.setOnClickListener {
            binding.btnReload.visibility = View.GONE
            webView.reload()
        }
    }


    override fun onDestroy() {
        webView.apply {
            clearHistory()
            clearCache(false)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

}
