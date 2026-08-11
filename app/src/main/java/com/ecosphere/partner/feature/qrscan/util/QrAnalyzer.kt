package com.ecosphere.partner.feature.qrscan.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage

class QrAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(

        BarcodeScannerOptions.Builder()

            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            )

            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(
        imageProxy: ImageProxy
    ) {

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)

            .addOnSuccessListener { barcodes ->

                val qrValue = barcodes
                    .firstOrNull()
                    ?.rawValue

                if (!qrValue.isNullOrBlank()) {
                    onQrDetected(qrValue)
                }
            }

            .addOnFailureListener {

                // Ignore
            }

            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}