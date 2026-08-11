package com.ecosphere.partner.feature.setting

import android.content.Context
import android.content.res.Configuration
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Layout
import android.util.DisplayMetrics
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityPrivacyPolicyBinding
import com.ecosphere.partner.feature.setting.model.CmsPageResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class PrivacyPolicy : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding
    private val viewModel: PrivacyPolicyViewModel by viewModels()

    private var type: String? = null

    override fun attachBaseContext(base: Context) {
        val configuration = Configuration(base.resources.configuration)

        //  Ignore system font scaling
        configuration.fontScale = 1.0f

        // (Optional but recommended) Ignore display size scaling
        configuration.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE

        val context = base.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        observeCmsData()
        viewModel.fetchCmsData()
    }

    private fun setupUI() {
        type = intent.getStringExtra("Type")
        binding.tvTitle.text = type

        binding.ivBack.setOnClickListener {
            finish()
            finishSlideActivity()
        }
        onBackPressedDispatcher.addCallback(this){
            finish()
            finishSlideActivity()
        }
    }

    private fun observeCmsData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cmsDataState.collect { state ->
                    when (state) {
                        is UIState.Idle -> Unit
                        is UIState.Loading -> ProgressDialogUtil.showLoadingProgress(this@PrivacyPolicy, lifecycleScope)
                        is UIState.Success -> {
                            ProgressDialogUtil.dismiss()
                            showCmsContent(state.data)
                            viewModel.resetUICMSDataState()
                        }
                        is UIState.Error -> {
                            ProgressDialogUtil.dismiss()
                            viewModel.resetUICMSDataState()
                            CommonMethods.showConfirmationDialog(
                                context = this@PrivacyPolicy,
                                title = "Error !",
                                message = state.message,
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "Ok",
                                onConfirm = {
                                    finish()
                                    finishSlideActivity()
                                }
                            )
                        }
                        is UIState.Empty -> {
                            ProgressDialogUtil.dismiss()
                            viewModel.resetUICMSDataState()
                        }
                    }
                }
            }
        }
    }

    private fun showCmsContent(response: CmsPageResponse) {

        val content = when (type) {
            "Privacy Policy" -> response.privacyPolicy
            "About Us" -> response.about
            "Terms & Conditions" -> response.termsConditions
            else -> ""
        }

        binding.tvCmsData.text = if (content.isNullOrEmpty()) {
            ""
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
            else
                Html.fromHtml(content)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.tvCmsData.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }
    }
}