package com.example.nutriscan.presentation.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.presentation.components.ErrorMessage
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.StatusChip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

// ==================== UI STATE + VIEWMODEL ====================

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class  Success(val scans: List<ScanResult>) : HistoryUiState
    data object Empty   : HistoryUiState
    data class  Error(val message: String) : HistoryUiState
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
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading
        )
}

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onScanSelected: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Scan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> LoadingIndicator()

            is HistoryUiState.Empty -> {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.History,
                        contentDescription = null,
                        modifier           = Modifier.size(64.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text     = "Belum ada riwayat scan",
                        style    = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp),
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is HistoryUiState.Success -> {
                LazyColumn(
                    modifier       = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.scans, key = { it.id }) { scan ->
                        HistoryScanCard(
                            scan    = scan,
                            onClick = { onScanSelected(scan.product.barcode) }
                        )
                    }
                }
            }

            is HistoryUiState.Error -> ErrorMessage(
                message = state.message,
                modifier = Modifier.padding(padding),
                onRetry = null
            )
        }
    }
}

@Composable
private fun HistoryScanCard(
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
                Text(
                    text  = formatTimestamp(scan.scannedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(8.dp))
            StatusChip(status = scan.analysis.overallStatus)
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val local   = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d/%02d/%d %02d:%02d".format(
        local.dayOfMonth, local.monthNumber, local.year,
        local.hour, local.minute
    )
}
