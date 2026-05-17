package com.example.nutriscan.core.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * iOS stub — barcode scanner via camera is not yet implemented for iOS.
 * Will be addressed in Sprint 3 using AVFoundation + expect/actual.
 */
@Composable
actual fun CameraBarcodeScannerView(
    onBarcodeDetected: (BarcodeScanResult) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier          = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            text  = "Scanner kamera belum tersedia di iOS",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
