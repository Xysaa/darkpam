package com.example.nutriscan.presentation.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.core.camera.BarcodeScanResult
import com.example.nutriscan.core.camera.CameraBarcodeScannerView
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.presentation.components.ErrorMessage
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.StatusChip
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBarcodeScanned: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val isScannerActive  by viewModel.isScannerActive.collectAsStateWithLifecycle()
    val showManualInput  by viewModel.showManualInput.collectAsStateWithLifecycle()
    val manualBarcode    by viewModel.manualBarcodeText.collectAsStateWithLifecycle()

    // ── Dialog input barcode manual ───────────────────────────────────────────
    if (showManualInput) {
        ManualBarcodeDialog(
            value     = manualBarcode,
            onChange  = viewModel::onManualBarcodeChange,
            onSubmit  = {
                val barcode = viewModel.submitManualBarcode()
                if (barcode != null) onBarcodeScanned(barcode)
            },
            onDismiss = viewModel::closeManualInput
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NutriScan") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Outlined.History, contentDescription = "Riwayat")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profil")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isScannerActive) {
                FloatingActionButton(
                    onClick = viewModel::openScanner,
                    shape   = CircleShape
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->

        if (isScannerActive) {
            // ── Full-screen camera scanner ────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                CameraBarcodeScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = { result ->
                        viewModel.closeScanner()
                        if (result is BarcodeScanResult.Success) {
                            onBarcodeScanned(result.barcode)
                        }
                    }
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    shape    = CircleShape,
                    color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    IconButton(onClick = viewModel::closeScanner) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup Scanner")
                    }
                }
                ScanGuideOverlay(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            // ── Main content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Scan card + tombol input manual berdampingan
                Row(
                    modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScanPromptCard(
                        onClick  = viewModel::openScanner,
                        modifier = Modifier.weight(1f)
                    )
                    ManualInputButton(onClick = viewModel::openManualInput)
                }

                when (val state = uiState) {
                    is HomeUiState.Loading -> LoadingIndicator()

                    is HomeUiState.Ready -> {
                        if (state.recentScans.isNotEmpty()) {
                            Text(
                                text     = "Scan Terakhir",
                                style    = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyColumn(
                                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.recentScans, key = { it.id }) { scan ->
                                    RecentScanCard(
                                        scan    = scan,
                                        onClick = { onBarcodeScanned(scan.product.barcode) }
                                    )
                                }
                            }
                        }
                    }

                    is HomeUiState.Error -> ErrorMessage(message = state.message, onRetry = null)

                    else -> Unit
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ScanPromptCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick  = onClick,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier              = Modifier.padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                modifier           = Modifier.size(40.dp),
                tint               = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column {
                Text(
                    text  = "Scan Produk",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = "Arahkan kamera ke barcode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ManualInputButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick  = onClick,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Edit,
                contentDescription = null,
                modifier           = Modifier.size(28.dp),
                tint               = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text  = "Manual",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ManualBarcodeDialog(
    value: String,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input Barcode Manual") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Masukkan nomor barcode produk (minimal 4 digit).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value          = value,
                    onValueChange  = onChange,
                    label          = { Text("Barcode") },
                    placeholder    = { Text("Contoh: 8991234567890") },
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = onSubmit,
                enabled  = value.length >= 4
            ) {
                Text("Cari Produk")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun RecentScanCard(
    scan: ScanResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = scan.product.displayName,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (scan.product.brand.isNotBlank()) {
                    Text(
                        text  = scan.product.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
            StatusChip(status = scan.analysis.overallStatus)
        }
    }
}

@Composable
private fun ScanGuideOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shape    = MaterialTheme.shapes.medium
    ) {
        Text(
            text     = "Arahkan kamera ke barcode produk",
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
