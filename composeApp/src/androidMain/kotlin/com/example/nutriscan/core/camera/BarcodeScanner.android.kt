package com.example.nutriscan.core.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "CameraBarcodeScannerView"

@Composable
actual fun CameraBarcodeScannerView(
    onBarcodeDetected: (BarcodeScanResult) -> Unit,
    modifier: Modifier
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Prevent firing the callback multiple times for the same scan session
    var isProcessing by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        runCatching {
                            val cameraProvider = cameraProviderFuture.get()

                            // ── Preview use-case ──────────────────────────────
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            // ── ML Kit barcode scanner ────────────────────────
                            val options = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                    Barcode.FORMAT_EAN_13,
                                    Barcode.FORMAT_EAN_8,
                                    Barcode.FORMAT_UPC_A,
                                    Barcode.FORMAT_UPC_E,
                                    Barcode.FORMAT_CODE_128,
                                    Barcode.FORMAT_CODE_39,
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_DATA_MATRIX
                                )
                                .build()
                            val scanner = BarcodeScanning.getClient(options)

                            // ── Image analysis use-case ───────────────────────
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { imageAnalysis ->
                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        if (!isProcessing) {
                                            processImageProxy(
                                                scanner      = scanner,
                                                imageProxy   = imageProxy,
                                                onSuccess    = { barcode ->
                                                    isProcessing = true
                                                    onBarcodeDetected(BarcodeScanResult.Success(barcode))
                                                },
                                                onFailure    = { error ->
                                                    Log.e(TAG, "ML Kit error: $error")
                                                    onBarcodeDetected(BarcodeScanResult.Error(error))
                                                }
                                            )
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            // ── Bind to lifecycle ─────────────────────────────
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        }.onFailure { e ->
                            Log.e(TAG, "Camera binding failed", e)
                            onBarcodeDetected(BarcodeScanResult.Error(e.message ?: "Kamera gagal dimulai"))
                        }
                    },
                    ContextCompat.getMainExecutor(ctx)
                )

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val firstValid = barcodes
                .firstOrNull { !it.rawValue.isNullOrBlank() }
                ?.rawValue
            if (firstValid != null) {
                onSuccess(firstValid)
            }
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Gagal memindai barcode")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
