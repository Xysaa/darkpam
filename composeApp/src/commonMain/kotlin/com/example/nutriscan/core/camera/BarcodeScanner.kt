package com.example.nutriscan.core.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-agnostic result from a barcode scan attempt.
 */
sealed interface BarcodeScanResult {
    data class Success(val barcode: String) : BarcodeScanResult
    data class Error(val message: String)   : BarcodeScanResult
    data object Cancelled                   : BarcodeScanResult
}

/**
 * expect composable — rendered differently per platform.
 *
 * On Android  → CameraX preview + ML Kit real-time analysis.
 * On iOS      → stub (Sprint 3+).
 *
 * @param onBarcodeDetected called exactly once when a barcode is confirmed.
 * @param modifier          standard Compose modifier forwarded to the camera view.
 */
@Composable
expect fun CameraBarcodeScannerView(
    onBarcodeDetected: (BarcodeScanResult) -> Unit,
    modifier: Modifier = Modifier
)
