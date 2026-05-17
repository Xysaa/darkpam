package com.example.nutriscan.presentation.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.NutritionAnalysis
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.domain.repository.UserProfileRepository
import com.example.nutriscan.domain.usecase.AnalyzeNutritionUseCase
import com.example.nutriscan.presentation.components.ErrorMessage
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.NutrientBar
import com.example.nutriscan.presentation.components.StatusChip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ==================== UI STATE + VIEWMODEL ====================

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class  Success(
        val product:  Product,
        val profile:  UserProfile,
        val analysis: NutritionAnalysis
    ) : ResultUiState
    data class  NoProfile(val barcode: String) : ResultUiState
    data class  Error(val message: String) : ResultUiState
}

class ResultViewModel(
    private val barcode: String,
    private val userProfileRepository: UserProfileRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val analyzeNutritionUseCase: AnalyzeNutritionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init { loadResult() }

    private fun loadResult() {
        viewModelScope.launch {
            try {
                // Check profile first
                val profile = userProfileRepository.getProfile().first()
                if (profile == null) {
                    _uiState.value = ResultUiState.NoProfile(barcode)
                    return@launch
                }

                // Check if we already have this barcode cached in history
                val cached = scanHistoryRepository.getScanById(barcode.toLongOrNull() ?: -1L)

                if (cached != null) {
                    _uiState.value = ResultUiState.Success(
                        product  = cached.product,
                        profile  = profile,
                        analysis = cached.analysis
                    )
                    return@launch
                }

                // Placeholder product — Sprint 3 will fetch from OpenFoodFacts API
                val product = Product(
                    barcode  = barcode,
                    name     = "Memuat produk...",
                    brand    = "",
                    imageUrl = ""
                )

                val analysis = analyzeNutritionUseCase(product, profile)

                _uiState.value = ResultUiState.Success(
                    product  = product,
                    profile  = profile,
                    analysis = analysis
                )
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun retry() {
        _uiState.value = ResultUiState.Loading
        loadResult()
    }
}

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    barcode: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(barcode) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Scan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ResultUiState.Loading -> LoadingIndicator()

            is ResultUiState.NoProfile -> {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(64.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Profil diperlukan untuk analisis",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Lengkapi profil Anda agar NutriScan bisa memberikan analisis yang dipersonalisasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is ResultUiState.Error -> ErrorMessage(
                message  = state.message,
                modifier = Modifier.padding(padding),
                onRetry  = viewModel::retry
            )

            is ResultUiState.Success -> ResultContent(
                product  = state.product,
                profile  = state.profile,
                analysis = state.analysis,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ResultContent(
    product: Product,
    profile: UserProfile,
    analysis: NutritionAnalysis,
    modifier: Modifier = Modifier
) {
    val serving = product.nutrimentsPerServing
    val n       = serving  // alias

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Product header ───────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.displayName, style = MaterialTheme.typography.titleLarge)
                if (product.brand.isNotBlank()) {
                    Text(product.brand, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Barcode: ${product.barcode}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Overall status ───────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Status untuk ${profile.name}",
                        style = MaterialTheme.typography.labelMedium)
                    Text("Per sajian ${product.servingSize.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(status = analysis.overallStatus)
            }
        }

        // ── Nutrient bars ────────────────────────────────────────────────────
        if (analysis.warnings.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Detail Nutrisi", style = MaterialTheme.typography.titleSmall)
                    analysis.warnings.forEach { warning ->
                        NutrientBar(
                            label        = warning.nutrientName,
                            value        = warning.valuePerServing,
                            unit         = warning.unit,
                            percentDaily = warning.percentDailyValue,
                            status       = warning.status
                        )
                    }
                }
            }
        }

        // ── Warnings ─────────────────────────────────────────────────────────
        if (analysis.warningMessages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Peringatan", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.height(8.dp))
                    analysis.warningMessages.forEach { msg ->
                        Text("• $msg", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // ── Sprint 3 placeholder ─────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text     = "ℹ️ Data nutrisi lengkap & saran AI akan tersedia di Sprint 3 setelah integrasi OpenFoodFacts API.",
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
