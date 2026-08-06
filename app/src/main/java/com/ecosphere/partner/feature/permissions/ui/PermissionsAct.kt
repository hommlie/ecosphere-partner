package com.ecosphere.partner.feature.permissions.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ecosphere.partner.feature.main.MainActivity
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.Constants
import com.ecosphere.partner.databinding.ActivityPermissionsBinding
import com.ecosphere.partner.feature.permissions.model.PermissionAction
import com.ecosphere.partner.feature.permissions.model.PermissionCard
import com.ecosphere.partner.feature.permissions.model.PermissionStatus
import com.ecosphere.partner.feature.permissions.model.PermissionType
import com.ecosphere.partner.feature.permissions.model.PermissionUiState
import com.ecosphere.partner.feature.permissions.viewmodel.PermissionsViewModel
import com.ecosphere.partner.feature.tracking.controller.TrackingController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PermissionsAct : AppCompatActivity() {
    private lateinit var binding : ActivityPermissionsBinding

    private val viewModel: PermissionsViewModel by viewModels()
    @Inject
    lateinit var trackingController: TrackingController

    private val runtimePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            val currentPermission =
                viewModel.uiState.value.currentPermission
                    ?: return@registerForActivityResult

            if (granted) {

                viewModel.onPermissionResult(
                    permanentlyDenied = false
                )

            } else {

                val permanentlyDenied =
                    isPermissionPermanentlyDenied(
                        currentPermission.type
                    )

                if (permanentlyDenied) {

                    showPermanentDeniedDialog()

                } else {

                    viewModel.onPermissionResult(
                        permanentlyDenied = false
                    )
                }
            }
        }

    private val settingsLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            viewModel.onReturnedFromSettings()

        }

    private val batteryOptimizationLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            viewModel.onReturnedFromSettings()

        }

    private val gpsLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            viewModel.onReturnedFromSettings()

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.setFrom(intent.getStringExtra(Constants.EXTRA_FROM) ?: "")

        observeUiState()

        observeActions()

        setupClickListeners()

        onBackPressedDispatcher.addCallback(this) {
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.refresh()
    }

    private fun setupClickListeners() {

        binding.primaryButton.setOnClickListener {
            viewModel.onPrimaryButtonClick()
        }
    }

    private fun observeUiState() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun observeActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actions.collect(::handleAction)
            }
        }
    }

    private fun renderCurrentPermission(
        permission: PermissionCard?
    ) {

        permission ?: return

        with(binding) {

            summaryList.visibility = View.GONE

            iconGlyph.setImageResource(permission.iconRes)
            titleText.setText(permission.titleRes)
            bodyText.setText(permission.descriptionRes)

            if (permission.noteRes != null) {

                noteText.visibility = View.VISIBLE

                noteText.setText(permission.noteRes)

            } else {
                noteText.visibility = View.GONE
            }
            primaryButton.text = getString(R.string.continue_text)
        }
    }

    private fun handleAction(
        action: PermissionAction
    ) {
        when (action) {

            is PermissionAction.RequestRuntimePermission -> {
                requestPermission(action.permission)
            }
            PermissionAction.OpenAppSettings -> {
                openAppSettings()
            }
            PermissionAction.OpenBatteryOptimizationSettings -> {
                openBatteryOptimization()
            }
            PermissionAction.OpenLocationSettings -> {
                openLocationSettings()
            }
            PermissionAction.ResumeTracking -> {
                trackingController.onLocationPermissionGranted()
            }
            PermissionAction.Finish -> {

                when(viewModel.from.value){
                    Constants.FROM_SPLASH -> {
                        openMain()
                    }
                    Constants.FROM_LOGIN ->{
                        openMain()
                    }
                    else -> {
                        finish()
                    }

                }
            }
        }
    }
    private fun openMain() {4

        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        })
    }

    private fun dpToPx(
        value: Int
    ): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    private fun requestPermission(
        permission: PermissionType
    ) {

        when (permission) {

            PermissionType.LOCATION -> {

                runtimePermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

            }

            PermissionType.BACKGROUND_LOCATION -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    runtimePermissionLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )

                } else {

                    viewModel.onPermissionResult(
                        permanentlyDenied = false
                    )

                }

            }

            PermissionType.NOTIFICATION -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    runtimePermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                } else {

                    viewModel.onPermissionResult(
                        permanentlyDenied = false
                    )
                }
            }
            PermissionType.BATTERY_OPTIMIZATION -> {
                showBatteryOptimizationDialog()
            }
            PermissionType.GPS -> {
                showLocationDialog()
            }
        }
    }

    private fun isPermissionPermanentlyDenied(
        permission: PermissionType
    ): Boolean {

        val androidPermission = when (permission) {

            PermissionType.LOCATION ->
                Manifest.permission.ACCESS_FINE_LOCATION

            PermissionType.BACKGROUND_LOCATION ->
                Manifest.permission.ACCESS_BACKGROUND_LOCATION

            PermissionType.NOTIFICATION ->
                Manifest.permission.POST_NOTIFICATIONS

            PermissionType.BATTERY_OPTIMIZATION ->
                return false
            PermissionType.GPS ->
                return false

        }

        return !ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            androidPermission
        )

    }

    private fun openAppSettings() {

        settingsLauncher.launch(

            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(
                    "package",
                    packageName,
                    null
                )
            )

        )

    }

    private fun openBatteryOptimization() {

        batteryOptimizationLauncher.launch(

            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ).apply {

                data = Uri.parse("package:$packageName")
            }
        )
    }

    private fun openLocationSettings() {
        gpsLauncher.launch(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        )
    }

    private fun showPermanentDeniedDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_permanently_denied_message)
            .setCancelable(false)
            .setPositiveButton(R.string.open_settings) { dialog, _ ->

                dialog.dismiss()

                openAppSettings()
            }
            .show()

    }

    private fun showBatteryOptimizationDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.disable_battery_optimization)
            .setMessage(R.string.disable_battery_optimization_message)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_text) { dialog, _ ->

                dialog.dismiss()
                openBatteryOptimization()
            }
            .show()

    }

    private fun showLocationDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enable_location)
            .setMessage(R.string.enable_location_message)
            .setCancelable(false)
            .setPositiveButton(R.string.open_settings) { dialog, _ ->
                dialog.dismiss()
                openLocationSettings()
            }
            .show()
    }

    private fun render(
        state: PermissionUiState
    ) {

        renderDots(state)

        if (state.isCompleted) {

            renderSummary(state.permissions)

        } else {

            renderCurrentPermission(
                state.currentPermission
            )

        }

    }
    private fun renderDots(
        state: PermissionUiState
    ) {

        binding.dotsContainer.removeAllViews()

        val totalSteps = state.permissions.size

        repeat(totalSteps) { index ->

            val permission = state.permissions[index]

            val segment = View(this)

            segment.layoutParams = LinearLayout.LayoutParams(
                0,
                dpToPx(4),
                1f
            ).apply {
                if (index != totalSteps - 1) {
                    marginEnd = dpToPx(6)
                }
            }

            val isCurrent =
                permission.type == state.currentPermission?.type

            val isGranted =
                permission.status == PermissionStatus.GRANTED ||
                        permission.status == PermissionStatus.NOT_REQUIRED

            val color = when {

                state.isCompleted ->
                    R.color.text_primary

                isCurrent ->
                    R.color.text_primary

                isGranted ->
                    R.color.text_primary

                else ->
                    R.color.divider

            }

            segment.setBackgroundColor(
                ContextCompat.getColor(this, color)
            )

            binding.dotsContainer.addView(segment)
        }

    }
    private fun renderSummary(
        permissions: List<PermissionCard>
    ) {

        with(binding) {

            summaryList.visibility = View.VISIBLE

            summaryList.removeAllViews()

            permissions.forEach { permission ->

                val item = layoutInflater.inflate(
                    R.layout.item_summary_row,
                    summaryList,
                    false
                )

                item.findViewById<TextView>(
                    R.id.rowLabel
                ).setText(permission.titleRes)

                item.findViewById<TextView>(
                    R.id.rowStatus
                ).apply {

                    text = getString(R.string.granted)

                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green_fg
                        )
                    )

                }

                summaryList.addView(item)

            }

            iconGlyph.setImageResource(
                R.drawable.ic_varify
            )

            titleText.setText(
                R.string.permission_complete_title
            )

            bodyText.setText(
                R.string.permission_complete_description
            )

            noteText.visibility = View.GONE

            primaryButton.text = getString(R.string.start_tracking)

        }

    }

}