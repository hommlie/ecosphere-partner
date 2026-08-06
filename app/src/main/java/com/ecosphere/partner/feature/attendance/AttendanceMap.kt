package com.ecosphere.partner.feature.attendance

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.common.getAddressFromLatLng
import com.ecosphere.partner.databinding.ActivityAttendanceMapBinding
import com.ecosphere.partner.feature.attendance.map.RouteRenderer
import com.ecosphere.partner.feature.attendance.model.TrackingSessionUi
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.common.getParcelableCompat
import com.ecosphere.partner.core.common.toKm
import com.ecosphere.partner.core.util.DateTimeUtils.toTime12Hour
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.model.TrackingStatus

@AndroidEntryPoint
class AttendanceMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAttendanceMapBinding
    private val viewModel : AttendanceMapViewModel by viewModels()

    private lateinit var renderer: RouteRenderer
    private var trackingSession: TrackingSessionUi? = null

    companion object {
        const val EXTRA_TRACKING_SESSION= "extra_tracking_session"
    }
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAttendanceMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        trackingSession = intent.getParcelableCompat<TrackingSessionUi>(EXTRA_TRACKING_SESSION)

        if (trackingSession == null) {
            CommonMethods.showConfirmationDialog(
                context = this,
                title = "Alert !",
                message = "Unable to get the session details.",
                isCancelable = false,
                showNegativeButton = false,
                positiveText = "OK",
                onConfirm = {
                    finish()
                    finishSlideActivity()
                }
            )
            return
        }

        setupToolbar()
        setupMap()

    }

    private fun setupToolbar() {
        binding.tvSessionCount.text = "Punch Session #${trackingSession?.sessionNumber}"
        binding.tvStartTime.text = trackingSession?.startTime?.toTime12Hour()
        binding.tvEndTime.text = if (trackingSession?.endTime == null) "- -" else trackingSession?.endTime?.toTime12Hour()
        binding.tvDuration.text = CommonMethods.calculateDuration(trackingSession?.startTime, trackingSession?.endTime)
        binding.tvDistance.text = trackingSession?.totalDistance?.toKm()

        when(trackingSession?.status){
            TrackingStatus.STARTED.name -> {
                binding.tvTripStatus.text = "Running"
                binding.tvTripStatus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_dark))
                binding.clTripStatus.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
                binding.liveIndicatorTripStatus.setDotColor(ContextCompat.getColor(this, R.color.green))
                binding.liveIndicatorTripStatus.start()
            }
            TrackingStatus.STOPPED.name -> {
                binding.tvTripStatus.text = "Ended"
                binding.tvTripStatus.setTextColor(ContextCompat.getColor(this, R.color.color_9C1335))
                binding.clTripStatus.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red_logout)
                binding.liveIndicatorTripStatus.setDotColor(ContextCompat.getColor(this, R.color.color_9C1335))
                binding.liveIndicatorTripStatus.stop()
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
            finishSlideActivity()
        }
        onBackPressedDispatcher.addCallback(this){
            finish()
            finishSlideActivity()
        }
    }
    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map
        ) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(
        map: GoogleMap
    ) {

        googleMap = map
        renderer = RouteRenderer(googleMap)
        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = false
        }
        observeUi()
        viewModel.getTrackingPoints(trackingSession?.sessionId.orEmpty())
    }

    private fun observeUi() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    when(state){
                        is UIState.Idle -> {}

                        is UIState.Loading -> {
                            ProgressDialogUtil.showAlertLoadingProgress(
                                this@AttendanceMap,
                                lifecycleScope,
                                "Loading...",
                                "Rendering session route..."
                            )
                        }
                        is UIState.Success -> {

                            renderer.render(state.data){
                                ProgressDialogUtil.dismiss()
                                setUpUiData(state.data)
                            }
                        }
                        is UIState.Empty -> {
                            ProgressDialogUtil.dismiss()
                            CommonMethods.showConfirmationDialog(
                                this@AttendanceMap,
                                "Alert !",
                                message = "No route found",
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "OK",
                                onConfirm = {}
                            )
                        }
                        is UIState.Error -> {
                            ProgressDialogUtil.dismiss()
                            CommonMethods.showConfirmationDialog(
                                this@AttendanceMap,
                                "Alert !",
                                message = state.message,
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "OK",
                                onConfirm = {
                                    finish()
                                    finishSlideActivity()
                                })
                        }
                    }
                }
            }
        }
    }

    private fun setUpUiData(points: List<TrackingPoint>) {

        if (points.isEmpty()) return

        val startPoint = points.first()

        getAddressFromLatLng(
            startPoint.latitude,
            startPoint.longitude
        ) {
            binding.tvStartAddress.post {
                binding.tvStartAddress.text = it
            }

        }

        if (trackingSession?.status == TrackingStatus.STARTED.name) {

            binding.tvEndAddress.text = "Trip is currently running"

        } else {

            val endPoint = points.last()

            getAddressFromLatLng(
                endPoint.latitude,
                endPoint.longitude
            ) {
                binding.tvEndAddress.post {
                    binding.tvEndAddress.text = it
                }
            }
        }
    }
}