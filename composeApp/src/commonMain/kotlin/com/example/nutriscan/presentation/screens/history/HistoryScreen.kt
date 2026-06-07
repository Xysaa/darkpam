package com.example.nutriscan.presentation.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.presentation.components.EmptyState
import com.example.nutriscan.presentation.components.ErrorMessage
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.ScanResultCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel

// ==================== UI STATE + VIEWMODEL ====================

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val scans: List<ScanResult>) : HistoryUiState
    data object Empty : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

class HistoryViewModel(
    repository: ScanHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.getAllHistory()
        .map { scans ->
            if (scans.isEmpty()) HistoryUiState.Empty
            else HistoryUiState.Success(scans)
        }
        .catch { e -> emit(HistoryUiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading
        )
}

// ==================== SCREEN ====================

@Composable
fun HistoryScreen(
    onScanSelected: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Riwayat Scan",
            subtitle = "Produk yang pernah kamu pindai"
        )

        when (val state = uiState) {
            is HistoryUiState.Loading -> LoadingIndicator()

            is HistoryUiState.Empty -> EmptyState(
                icon = Icons.Filled.Inventory2,
                title = "Belum ada riwayat",
                subtitle = "Pindai produk pertamamu dengan tombol scan di bawah.",
                modifier = Modifier.fillMaxSize()
            )

            is HistoryUiState.Success -> {
                Text(
                    text = "${state.scans.size} produk",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.scans, key = { it.id }) { scan ->
                        ScanResultCard(
                            scan = scan,
                            onClick = { onScanSelected(scan.product.barcode) },
                            showTime = true
                        )
                    }
                }
            }

            is HistoryUiState.Error -> ErrorMessage(message = state.message, onRetry = null)
        }
    }
}
