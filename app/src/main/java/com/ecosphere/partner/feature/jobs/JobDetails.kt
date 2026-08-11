package com.ecosphere.partner.feature.jobs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.common.getParcelableCompat
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.common.toCapOnlyFirstLetter
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityJobDetailsBinding
import com.ecosphere.partner.feature.jobs.adapter.JobDetailsWasteAdapter
import com.ecosphere.partner.feature.jobs.`interface`.CapturePhotoUi
import com.ecosphere.partner.feature.jobs.`interface`.WasteActionListener
import com.ecosphere.partner.feature.jobs.model.PickUpDataByQR
import com.ecosphere.partner.feature.jobs.model.SubmitWasteCollectionRequest
import com.ecosphere.partner.feature.jobs.model.WasteCollectionItemRequest
import com.ecosphere.partner.feature.jobs.model.WasteCollectionUi
import com.ecosphere.partner.feature.jobs.model.WasteType
import com.ecosphere.partner.feature.login.ui.LoginAct
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class JobDetails : AppCompatActivity() {
    private lateinit var binding : ActivityJobDetailsBinding
    private val viewModel : JobDetailsViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    private lateinit var jobDetailsWasteAdapter: JobDetailsWasteAdapter

    companion object {
        const val EXTRA_PICKUP_DATA = "extra_pickup_data"
    }

    private var cameraImageUri: Uri? = null
    private var currentWastePosition = -1
    private val MAX_IMAGES_PER_WASTE = 5

    private var pickUpQrData : PickUpDataByQR ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJobDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                maxOf(systemBars.bottom, ime.bottom))
            insets
        }

        binding.ivBack.setOnClickListener {
            finish()
            finishSlideActivity()
        }
        onBackPressedDispatcher.addCallback(this){
            finish()
            finishSlideActivity()
        }
        pickUpQrData = intent.getParcelableCompat<PickUpDataByQR>(EXTRA_PICKUP_DATA)
        if (pickUpQrData == null){
            CommonMethods.showConfirmationDialog(
                context = this,
                title = "Alert !",
                message = "Unable to get the pickup details\nPlease try again later",
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

        observeSessionExpired()

        binding.tvCustomerName.text = pickUpQrData?.apartmentName?.toCapOnlyFirstLetter()
        binding.tvAllPickupId.text = "${pickUpQrData?.orderId} • ${pickUpQrData?.vehicleNumber}"

        jobDetailsWasteAdapter = JobDetailsWasteAdapter(

            object : WasteActionListener {

                override fun onAddPhoto(
                    wastePosition: Int
                ) {

                    val item = jobDetailsWasteAdapter.getItem(wastePosition) ?: return

                    if (item.images.size >= MAX_IMAGES_PER_WASTE) {

                        Toast.makeText(
                            this@JobDetails,
                            "Maximum 5 photos allowed.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return
                    }

                    currentWastePosition = wastePosition

                    requestCameraPermission()
                }

                override fun onRemovePhoto(
                    wastePosition: Int,
                    photo: CapturePhotoUi.Photo
                ) {

                    jobDetailsWasteAdapter.removePhoto(
                        wastePosition,
                        photo.uri
                    )
                }
            }
        )
        observeSubmitCollection()
        setUpWasteCollectionAdapter()


        val wasteItems = pickUpQrData?.subcategories
            ?.map { subCategory ->
                WasteCollectionUi(
                    categoryId = subCategory.categoryId ?: 0,
                    categoryName = subCategory.categoryName.orEmpty(),
                    subCategoryId = subCategory.subcategoryId ?: 0,
                    wasteType = WasteType.fromDisplayName(subCategory.subcategoryName),
                    images = mutableListOf()
                )
            }
            .orEmpty()

        jobDetailsWasteAdapter.submitItems(wasteItems.reversed())

        binding.btnSubmitCollection.setOnClickListener {

            if (!validate()) return@setOnClickListener

            lifecycleScope.launch {

                val wasteItems = jobDetailsWasteAdapter.getItems()

                val request = SubmitWasteCollectionRequest(
                    orderId = pickUpQrData?.orderId.orEmpty(),
                    vehicleId = pickUpQrData?.vehicleId ?: 0,
                    driverId = sessionManager.getDriverId().toString().toInt(),
                    remarks = "Morning collection completed",

                    items = wasteItems.map { item ->

                        WasteCollectionItemRequest(
                            categoryId = item.categoryId,
                            subCategoryId = item.subCategoryId,
                            subCategoryName = item.wasteType.displayName,
                            totalWasteKg = item.weight
                                .toDoubleOrNull()
                                ?: 0.0
                        )
                    }
                )



                Log.d(
                    "SubmitWasteRequest",
                    Gson().toJson(request)
                )
                viewModel.submitPickUpCollection(request)
            }
        }

    }
    private fun validate(): Boolean {

        val data =
            jobDetailsWasteAdapter.getItems()

        data.forEach {

            if (it.weight.isBlank()) {

                Toast.makeText(
                    this,
                    "Please enter ${it.wasteType.displayName} weight",
                    Toast.LENGTH_SHORT
                ).show()

                return false
            }
        }
        return true
    }

    private fun setUpWasteCollectionAdapter(){
        binding.rvWasteCollection.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jobDetailsWasteAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    private fun requestCameraPermission() {

        when {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {

                launchCameraInternal()
            }

            else -> {
                showCameraPermissionDialog()
            }
        }
    }
    private fun showCameraPermissionDialog() {

        CommonMethods.showConfirmationDialog(
            context = this,
            title = "Camera Permission Required",
            message = "Camera permission has been permanently denied.\n\n" +
                        "Please enable it from App Settings to continue.\n\n" +
                        "Steps:\n" +
                        "• Tap 'Open Settings'.\n" +
                        "• Select 'Permissions'.\n" +
                        "• Turn on the 'Camera' permission.\n" +
                        "• Return to the app and capture the photo again.",
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



    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                launchCameraInternal()

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    showCameraPermissionDialog()
                } else {
                    showPermissionSettingsDialog()
                }
            }
        }
    private fun showPermissionSettingsDialog() {

        CommonMethods.showConfirmationDialog(
            context = this,
            title = "Camera Permission Required",
            message = "Camera permission has been permanently denied.\n\nPlease enable it from App Settings.",
            isCancelable = false,
            showNegativeButton = true,
            positiveText = "Open Settings",
            negativeText = "Cancel",
            onConfirm = {

                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts(
                        "package",
                        packageName,
                        null
                    )
                )

                startActivity(intent)
            }
        )
    }

    private fun launchCameraInternal() {

        val imageFile = File.createTempFile(
            "pickup_${System.currentTimeMillis()}",
            ".jpg",
            cacheDir
        )

        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            imageFile
        )

        cameraImageUri = uri

        takePhotoLauncher.launch(uri)
    }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {

                lifecycleScope.launch {
                    ProgressDialogUtil.showAlertLoadingProgress(
                        this@JobDetails,
                        lifecycleScope,
                        "Processing...",
                        "Please wait while we are compressing your image"
                    )

                    val sourceUri = cameraImageUri ?: return@launch

                    val compressedUri = withContext(Dispatchers.IO) {
                        CommonMethods.compressImageFromUri(
                            this@JobDetails,
                            sourceUri
                        )
                    }

                    if (compressedUri == null) {
                        Toast.makeText(
                            this@JobDetails,
                            "Unable to process image.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    jobDetailsWasteAdapter.addPhoto(
                        currentWastePosition,
                        compressedUri
                    )

                    ProgressDialogUtil.dismiss()
                }
            }
        }

    private fun observeSubmitCollection(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.submitPickUpCollection.collect{ state ->
                    when(state){
                        is UIState.Idle -> {}

                        is UIState.Loading -> {
                            ProgressDialogUtil.showAlertLoadingProgress(
                                this@JobDetails,
                                lifecycleScope,
                                "Processing...",
                                "Please wait while we are submitting your collection"
                            )
                        }
                        is UIState.Success -> {
                            viewModel.resetSubmitPickUpCollection()
                            CommonMethods.showSuccessDialog(
                                this@JobDetails,
                                "Thank you\nPickup collected successfully"
                            ) {
                                finish()
                                finishSlideActivity()
                            }
                        }
                        is UIState.Empty -> {
                            viewModel.resetSubmitPickUpCollection()
                        }
                        is UIState.Error -> {
                            viewModel.resetSubmitPickUpCollection()
                            CommonMethods.showConfirmationDialog(
                                context = this@JobDetails,
                                title = "Alert !",
                                message = state.message,
                                isCancelable = false,
                                showNegativeButton = false,
                                positiveText = "OK",
                                onConfirm = {
                                }
                            )
                        }
                    }
                }

            }
        }
    }

    private fun observeSessionExpired() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                sessionManager.sessionExpired.collect {

                    Toast.makeText(
                        this@JobDetails,
                        "Session expired. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()

                    startSlideActivity(
                        Intent(this@JobDetails, LoginAct::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            }
        }
    }
}