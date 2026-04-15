package com.gymku.app.ui.screens.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

/**
 * ImageAnalysis.Analyzer that processes each camera frame with ML Kit barcode scanning.
 * The @ExperimentalGetImage annotation is placed at function level to avoid issues.
 */
class QrCodeAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.rawValue
                    ?.let { onQrCodeScanned(it) }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
