package com.example.nutriscan.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ==================== UI STATE ====================

sealed interface HomeUiState {
    data object Idle    : HomeUiState
    data object Loading : HomeUiState
    data class  Ready(val recentScans: List<ScanResult>) : HomeUiState
    data class  Error(val message: String) : HomeUiState
}

// ==================== VIEWMODEL ====================

class HomeViewModel(
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {

    // ── Scanner state ────────────────────────────────────────────────────────
    private val _isScannerActive = MutableStateFlow(false)
    val isScannerActive: StateFlow<Boolean> = _isScannerActive.asStateFlow()

    // ── Manual barcode input state ───────────────────────────────────────────
    private val _showManualInput = MutableStateFlow(false)
    val showManualInput: StateFlow<Boolean> = _showManualInput.asStateFlow()

    private val _manualBarcodeText = MutableStateFlow("")
    val manualBarcodeText: StateFlow<String> = _manualBarcodeText.asStateFlow()

    // ── Recent scans ─────────────────────────────────────────────────────────
    val uiState: StateFlow<HomeUiState> = scanHistoryRepository
        .getRecentHistory(limit = 5)
        .map<List<ScanResult>, HomeUiState> { HomeUiState.Ready(it) }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    // ── Camera scanner ───────────────────────────────────────────────────────
    fun openScanner()  { _isScannerActive.value = true  }
    fun closeScanner() { _isScannerActive.value = false }

    // ── Manual input ─────────────────────────────────────────────────────────
    fun openManualInput()  {
        _manualBarcodeText.value = ""
        _showManualInput.value   = true
    }
    fun closeManualInput() { _showManualInput.value = false }

    fun onManualBarcodeChange(text: String) {
        // Hanya angka dan huruf, maks 20 karakter
        _manualBarcodeText.value = text.filter { it.isLetterOrDigit() }.take(20)
    }

    /** Mengembalikan barcode yang valid, atau null jika terlalu pendek */
    fun submitManualBarcode(): String? {
        val barcode = _manualBarcodeText.value.trim()
        return if (barcode.length >= 4) {
            closeManualInput()
            barcode
        } else null
    }
}
