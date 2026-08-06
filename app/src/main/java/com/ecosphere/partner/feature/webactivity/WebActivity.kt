package com.ecosphere.partner.feature.webactivity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.common.finishSlideBottomActivity
import com.ecosphere.partner.core.common.getParcelableCompat
import com.ecosphere.partner.core.common.loadSmartUrl
import com.ecosphere.partner.core.common.resetHistoryOnNextLoad
import com.ecosphere.partner.core.common.setupWebView
import com.ecosphere.partner.core.common.toCapWords
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityWebBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@AndroidEntryPoint
class WebActivity : AppCompatActivity() {
    private lateinit var binding : ActivityWebBinding

    private var webView: WebView?=null
    private lateinit var reportData: Reports
    private var url : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        url = intent.getStringExtra("url")?:""

        if (!url.isNullOrEmpty()){
            binding.tvTitle.visibility = View.GONE
            binding.mcvReportType.visibility = View.GONE
        }else {
            binding.tvTitle.visibility = View.GONE
            binding.mcvReportType.visibility = View.GONE
            reportData = intent.extras.getParcelableCompat<Reports>("report_data") ?: run {
                Toast.makeText(this, "Report data missing", Toast.LENGTH_SHORT).show()
                return
            }
        }
        binding.ivBack.setOnClickListener {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                finish()
                finishSlideActivity()
            }
        }
        if (url?.endsWith(".pdf", true) == true) {

            binding.webViewContainer.visibility = View.GONE
            binding.pdfView.visibility = View.VISIBLE

            ProgressDialogUtil.showAlertLoadingProgress(
                this,
                lifecycleScope,
                "Report",
                "Downloading PDF, please wait..."
            )
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val file = downloadPdf(url!!)

                    ProgressDialogUtil.dismiss()
                    withContext(Dispatchers.Main) {
                        binding.pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .load()
                    }
                } catch (e: Exception) {
                    ProgressDialogUtil.dismiss()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@WebActivity,
                            "Failed to load PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                    finishSlideBottomActivity()
                }
            }
            return
        }


        if (!isWebViewAvailable(this)) {
            binding.btnReload.visibility = View.VISIBLE
            Toast.makeText(
                this,
                "Android WebView is missing. Please update Chrome or Android System WebView.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        try {
            webView = WebView(this)
        } catch (e: Exception) {
            Toast.makeText(this, "WebView not available on this device", Toast.LENGTH_LONG).show()
            finish()
        }


        binding.webViewContainer.addView(webView)
        webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView?.setupWebView(this, lifecycleScope,binding.btnReload)

        binding.apply {
            try {
                if (url.isNullOrEmpty()) {
                    val firstName = reportData.reportUserFirstName?.toCapWords() ?: ""
                    val lastName = reportData.reportUserLastName?.toCapWords() ?: ""
                    tvTitle.text = "Report For - $firstName $lastName"
                    webView?.loadSmartUrl(this@WebActivity, reportData.healthscibeReportlink)
                } else {
                    webView?.loadSmartUrl(this@WebActivity, url)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@WebActivity, "WebView not available on this device", Toast.LENGTH_LONG).show()
            }




            // Doctor report click
            mcvReportTypeDoctor.setOnClickListener {
                setCardSelection(mcvReportTypeDoctor, mcvReportTypeOctor)
                webView?.stopLoading()
                webView?.resetHistoryOnNextLoad = true
                webView?.loadSmartUrl(this@WebActivity, reportData.healthscibeReportlink)

            }

            // Octor report click
            mcvReportTypeOctor.setOnClickListener {
                setCardSelection(mcvReportTypeOctor, mcvReportTypeDoctor)
                webView?.stopLoading()
                webView?.resetHistoryOnNextLoad = true
                webView?.loadSmartUrl(this@WebActivity, reportData.reportLinkHtml)

            }
        }

        binding.btnReload.setOnClickListener {
            binding.btnReload.visibility = View.GONE
            webView?.reload()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                finish()
                finishSlideActivity()
            }
        }

    }

    private fun downloadPdf(url: String): File {
        val connection = URL(url).openConnection().apply {
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        connection.connect()

        val input = BufferedInputStream(connection.getInputStream())
        val file = File(cacheDir, "temp_report.pdf")
        val output = FileOutputStream(file)

        val buffer = ByteArray(1024)
        var count: Int

        while (input.read(buffer).also { count = it } != -1) {
            output.write(buffer, 0, count)
        }

        output.flush()
        output.close()
        input.close()

        return file
    }


    private fun setCardSelection(selected: MaterialCardView, unselected: MaterialCardView) {
        selected.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        unselected.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_DDE4FF))
    }

    override fun onDestroy() {
        webView?.apply {
            clearHistory()
            clearCache(false)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
        webView = null
        super.onDestroy()
    }
    private fun isWebViewAvailable(context: Context): Boolean {
        return try {
            WebView.getCurrentWebViewPackage() != null
        } catch (e: Exception) {
            false
        }
    }


}