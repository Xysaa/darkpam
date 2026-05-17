package com.example.nutriscan.core.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Runtime permission state ──────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionDenied    = !granted
    }

    // Request permission on first composition if not yet granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── Permission denied UI ──────────────────────────────────────────────────
    if (permissionDenied) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(32.dp)
            ) {
                Text(
                    text      = "Izin kamera diperlukan untuk memindai barcode.",
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Izinkan Kamera")
                }
            }
        }
        return
    }

    // ── Waiting for permission grant ─────────────────────────────────────────
    if (!hasCameraPermission) return

    // ── Camera preview ────────────────────────────────────────────────────────
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

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

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

                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { imageAnalysis ->
                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        if (!isProcessing) {
                                            processImageProxy(
                                                scanner   = scanner,
                                                imageProxy = imageProxy,
                                                onSuccess = { barcode ->
                                                    isProcessing = true
                                                    onBarcodeDetected(BarcodeScanResult.Success(barcode))
                                                },
                                                onFailure = { error ->
                                                    Log.e(TAG, "ML Kit error: $error")
                                                    onBarcodeDetected(BarcodeScanResult.Error(error))
                                                }
                                            )
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

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
