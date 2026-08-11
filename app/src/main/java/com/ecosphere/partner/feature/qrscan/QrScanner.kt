package com.ecosphere.partner.feature.qrscan

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityQrScannerBinding
import com.ecosphere.partner.feature.jobs.JobDetails
import com.ecosphere.partner.feature.jobs.JobDetailsViewModel
import com.ecosphere.partner.feature.login.ui.LoginAct
import com.ecosphere.partner.feature.qrscan.model.QrPickupData
import com.ecosphere.partner.feature.qrscan.util.QrAnalyzer
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class QrScanner : AppCompatActivity() {
    private lateinit var binding : ActivityQrScannerBinding
    private val viewModel : JobDetailsViewModel by viewModels()

    companion object{
        private const val FLASH_ON_LUX = 20f
        private const val FLASH_OFF_LUX = 40f
    }

    @Inject lateinit var sessionManager: SessionManager

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var imageAnalyzer: ImageAnalysis
    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    private lateinit var scanAnimator: ObjectAnimator

    private var isScanning = true
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var userChangedFlash = false
    private var flashEnabled = false
    private var ambientLux: Float? = null
    private var flashJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        CommonMethods.setNavigationBarColor(this,R.color.transparent,false)

        binding.ivBack.setOnClickListener {
            finish()
            finishSlideActivity()
        }
        onBackPressedDispatcher.addCallback(this){
            finish()
            finishSlideActivity()
        }

        val statusBarHeight = CommonMethods.getStatusBarHeight(this)
        binding.viewStatusBar.layoutParams.height = statusBarHeight

        sensorManager =
            getSystemService(
                SENSOR_SERVICE
            ) as SensorManager

        lightSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_LIGHT
            )

        binding.btnFlash.setOnClickListener {

            if (!::camera.isInitialized)
                return@setOnClickListener

            userChangedFlash = true
            flashEnabled = !flashEnabled

            camera.cameraControl.enableTorch(
                flashEnabled
            )

            binding.btnFlash.setImageResource(
                if (flashEnabled)
                    R.drawable.ic_flash_on
                else
                    R.drawable.ic_flash
            )
        }

        observeSessionExpired()
        observePickUpData()
        requestCameraPermission()

    }

    private fun observePickUpData(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.pickUpDataByQr.collect { state ->
                    when(state){
                        is UIState.Idle -> {
                            // no-op
                        }
                        is UIState.Loading -> {
                            ProgressDialogUtil.showAlertLoadingProgress(this@QrScanner, lifecycleScope, "Loading...", "Please wait while we are getting your pickup details")
                        }
                        is UIState.Empty -> {
                            ProgressDialogUtil.dismiss()
                            viewModel.resetPickUpDataByQr()
                            CommonMethods.showConfirmationDialog(
                                context = this@QrScanner,
                                title = "Alert !",
                                message = "Right now pickup not scheduled for this building.",
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "OK",
                                onConfirm = {
                                    userChangedFlash = false
                                    isScanning = true
                                    startCamera()
                                }
                            )
                        }
                        is UIState.Success -> {
                            ProgressDialogUtil.dismiss()
                            cameraProvider.unbindAll()
                            viewModel.resetPickUpDataByQr()
                            startSlideActivity(Intent(this@QrScanner, JobDetails::class.java).apply {
                                putExtra(JobDetails.EXTRA_PICKUP_DATA, state.data)
                            })
                            finish()
                        }
                        is UIState.Error -> {
                            ProgressDialogUtil.dismiss()
                            viewModel.resetPickUpDataByQr()
                            CommonMethods.showConfirmationDialog(
                                context = this@QrScanner,
                                title = "Error !",
                                message = state.message,
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "OK",
                                onConfirm = {
                                    userChangedFlash = false
                                    isScanning = true
                                    startCamera()
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    private fun startCamera() {

        val future = ProcessCameraProvider.getInstance(this)

        future.addListener({

            try {

                cameraProvider = future.get()

                bindCameraUseCases()

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Unable to start camera.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun bindCameraUseCases() {

        val preview = Preview.Builder()
            .setTargetRotation(
                binding.previewView.display.rotation
            )
            .build()

        preview.surfaceProvider =
            binding.previewView.surfaceProvider

        imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(
                    binding.previewView.display.rotation
                )
                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )
                .build()

        imageAnalyzer.setAnalyzer(
            cameraExecutor,
            QrAnalyzer(
                onQrDetected = ::onQrDetected
            )
        )

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(
                this,
                selector,
                preview,
                imageAnalyzer
            )

//        enableFlashIfRequired()

        binding.previewView.postDelayed({
            if (lightSensor != null) {
                autoUpdateFlash()
            } else {
                autoUpdateFlashByTime()
            }
        },300)

        startScanAnimation()
        startFramePulse()
    }

    private fun onQrDetected(
        qr: String
    ) {
        if (!isScanning) return

        isScanning = false
        stopAnimation()
//        cameraProvider.unbind(imageAnalyzer)
        imageAnalyzer.clearAnalyzer()

        runOnUiThread {
            vibrateSuccess()
            playSuccessBeep()

            val orderId = try {
                Gson().fromJson(qr, QrPickupData::class.java)
                    .orderId
            } catch (_: Exception) {
                qr
            }

            lifecycleScope.launch {
                viewModel.getPickUpDataByQr(
                    hashMapOf(
                        "order_id" to orderId,
                        "vehicle_id" to sessionManager.getDriverId().toString()
                    )
                )
            }
        }
    }

    private fun startScanAnimation() {

        binding.viewScanLine.post {

            val rect = binding.scannerOverlay.getScannerRect()

            val travelDistance =
                rect.height() -
                        binding.viewScanLine.height -
                        dp(16f)

            binding.viewScanLine.translationY = -travelDistance / 2f

            if (::scanAnimator.isInitialized) {
                scanAnimator.cancel()
            }

            scanAnimator =
                ObjectAnimator.ofFloat(
                    binding.viewScanLine,
                    View.TRANSLATION_Y,
                    -travelDistance / 2f,
                    travelDistance / 2f
                ).apply {

                    duration = 1800
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    interpolator = LinearInterpolator()

                    start()
                }
        }
    }
    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }
    private fun startFramePulse() {

        binding.scannerOverlay.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setDuration(900)
            .withEndAction {

                binding.scannerOverlay.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(900)
                    .withEndAction {
                        startFramePulse()
                    }
                    .start()
            }
            .start()
    }
    private fun stopAnimation() {
        if (::scanAnimator.isInitialized) {
            scanAnimator.cancel()
        }
    }

    private fun enableFlashIfRequired() {

        if (!shouldEnableFlashAutomatically())
            return

        binding.previewView.postDelayed({

            if (!::camera.isInitialized)
                return@postDelayed

            flashEnabled = true

            camera.cameraControl.enableTorch(true)

            binding.btnFlash.setImageResource(
                R.drawable.ic_flash_on
            )

        }, 500)
    }

    private fun shouldEnableFlashAutomatically(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 18 || hour <= 6
    }

    private fun applyFlashState() {

        if (!::camera.isInitialized)
            return

        camera.cameraControl.enableTorch(flashEnabled)

        binding.btnFlash.setImageResource(
            if (flashEnabled)
                R.drawable.ic_flash_on
            else
                R.drawable.ic_flash
        )
    }

//    private val lightListener = object : SensorEventListener {
//
//        override fun onSensorChanged(event: SensorEvent) {
//
//            if (event.sensor.type != Sensor.TYPE_LIGHT)
//                return
//
//            val now = SystemClock.elapsedRealtime()
//
//            if (now - lastSensorUpdate < 700)
//                return
//
//            lastSensorUpdate = now
//
//            val lux = event.values[0]
//
//            if (lastAppliedLux >= 0 && abs(lastAppliedLux - lux) < 8f)
//                return
//
//            lastAppliedLux = lux
//
//            ambientLux = lux
//
//            if (userChangedFlash)
//                return
//
//            autoUpdateFlash()
//        }
//
//        override fun onAccuracyChanged(
//            sensor: Sensor?,
//            accuracy: Int
//        ) = Unit
//    }

    private val lightListener = object : SensorEventListener {

        override fun onSensorChanged(
            event: SensorEvent
        ) {

            if (event.sensor.type != Sensor.TYPE_LIGHT)
                return

            ambientLux = event.values[0]
        }

        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int
        ) = Unit
    }
    private fun autoUpdateFlash() {

        if (!::camera.isInitialized)
            return

        val lux = ambientLux ?: return

        when {

            !flashEnabled && lux <= FLASH_ON_LUX -> {

                flashEnabled = true
                applyFlashState()
            }
            flashEnabled && lux >= FLASH_OFF_LUX -> {
                flashEnabled = false
                applyFlashState()
            }
        }
    }
    private fun autoUpdateFlashByTime() {

        if (!::camera.isInitialized)
            return
        flashEnabled = shouldEnableFlashAutomatically()
        applyFlashState()
    }

    private fun startAutoFlashMonitoring() {

        flashJob?.cancel()

        flashJob = lifecycleScope.launch {

            while (isActive) {

                if (!userChangedFlash) {

                    if (lightSensor != null) {
                        autoUpdateFlash()
                    } else {
                        autoUpdateFlashByTime()
                    }
                }

                delay(700)
            }
        }
    }
    private fun stopAutoFlashMonitoring() {
        flashJob?.cancel()
    }

    private fun requestCameraPermission() {

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {

                startCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {

                CommonMethods.showConfirmationDialog(
                    context = this,
                    title = "Camera Permission Required",
                    message = "Camera permission is required to scan QR codes.\n\nTap 'Allow' to continue.",
                    isCancelable = false,
                    showNegativeButton = true,
                    positiveText = "Allow",
                    negativeText = "Cancel",
                    onConfirm = {
                        cameraPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                )
            }

            else -> {
                cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }
    private val cameraPermissionLauncher : ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                startCamera()

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {

                    CommonMethods.showConfirmationDialog(
                        context = this,
                        title = "Camera Permission Required",
                        message = "Camera permission is required to scan QR codes.\n\nPlease tap 'Allow' to continue.",
                        isCancelable = false,
                        showNegativeButton = true,
                        positiveText = "Allow",
                        negativeText = "Cancel",
                        onConfirm = {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    )

                } else {

                    CommonMethods.showConfirmationDialog(
                        context = this,
                        title = "Camera Permission Required",
                        message =
                            "Camera permission has been permanently denied.\n\n" +
                                    "To scan QR codes:\n\n" +
                                    "1. Tap 'Open Settings'.\n" +
                                    "2. Open 'Permissions'.\n" +
                                    "3. Allow the 'Camera' permission.\n" +
                                    "4. Return to the app and scan again.",
                        isCancelable = false,
                        showNegativeButton = true,
                        positiveText = "Open Settings",
                        negativeText = "Cancel",
                        onConfirm = {

                            startActivity(
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
                    )
                }
            }
        }
    private fun vibrateSuccess() {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                            as VibratorManager

                val vibrator = vibratorManager.defaultVibrator

                if (!vibrator.hasVibrator()) return

                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        VibrationEffect.EFFECT_HEAVY_CLICK
                    )
                )

            } else {

                @Suppress("DEPRECATION")
                val vibrator =
                    getSystemService(Context.VIBRATOR_SERVICE)
                            as Vibrator

                if (!vibrator.hasVibrator()) return

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    vibrator.vibrate(
                        VibrationEffect.createPredefined(
                            VibrationEffect.EFFECT_HEAVY_CLICK
                        )
                    )

                } else {

                    @Suppress("DEPRECATION")
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            180,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                }
            }

        } catch (_: SecurityException) {

            // Permission missing

        } catch (_: Exception) {

            // Ignore
        }
    }

    private fun playSuccessBeep() {

        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(
            this, R.raw.qr_success_beep
        ).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setVolume(1f, 1f)
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
            start()
        }
    }

    override fun onResume() {
        super.onResume()

        isScanning = true

        if (::cameraProvider.isInitialized) {
            bindCameraUseCases()

        } else {
            startCamera()
        }

        userChangedFlash = false

        if (lightSensor != null) {
            sensorManager.registerListener(
                lightListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            autoUpdateFlashByTime()
        }
        startAutoFlashMonitoring()
    }
    override fun onPause() {
        stopAutoFlashMonitoring()
        sensorManager.unregisterListener(lightListener)

        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
        super.onPause()
    }
    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null

        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
        stopAnimation()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun observeSessionExpired() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                sessionManager.sessionExpired.collect {

                    Toast.makeText(
                        this@QrScanner,
                        "Session expired. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()

                    startSlideActivity(
                        Intent(this@QrScanner, LoginAct::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            }
        }
    }

}