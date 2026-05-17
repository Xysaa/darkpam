package com.example.nutriscan.presentation.screens.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.example.nutriscan.domain.model.NutritionAnalysis
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.AIRepository
import com.example.nutriscan.domain.repository.ProductRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ==================== UI STATE ====================

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(
        val product: Product,
        val profile: UserProfile,
        val analysis: NutritionAnalysis,
        val isAiLoading: Boolean = false     // saran AI masih dimuat
    ) : ResultUiState
    data class NoProfile(val barcode: String) : ResultUiState
    data class Error(val message: String) : ResultUiState
}

// ==================== VIEWMODEL ====================

class ResultViewModel(
    private val barcode: String,
    private val userProfileRepository: UserProfileRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val productRepository: ProductRepository,
    private val analyzeNutritionUseCase: AnalyzeNutritionUseCase,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init { loadResult() }

    private fun loadResult() {
        viewModelScope.launch {
            try {
                // 1. Cek profil
                val profile = userProfileRepository.getProfile().first()
                if (profile == null) {
                    _uiState.value = ResultUiState.NoProfile(barcode)
                    return@launch
                }

                // 2. Fetch produk (cache lokal → OpenFoodFacts API → dummy fallback)
                val product = productRepository.getProductByBarcode(barcode).getOrThrow()

                // 3. Analisis nutrisi lokal (selalu hitung ulang, pakai data profil terkini)
                val analysis = analyzeNutritionUseCase(product, profile)

                // 4. Tampilkan hasil segera, AI suggestion menyusul
                _uiState.value = ResultUiState.Success(
                    product     = product,
                    profile     = profile,
                    analysis    = analysis,
                    isAiLoading = true
                )

                // 5. Simpan ke history
                scanHistoryRepository.saveScan(
                    ScanResult(
                        product   = product,
                        analysis  = analysis,
                        scannedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )

                // 6. Minta saran AI — selalu dipanggil, tidak peduli dari cache atau API
                val aiResult     = aiRepository.analyzeNutrition(product, profile)
                val aiSuggestion = aiResult.getOrNull()
                // Tampilkan pesan error Gemini ke user jika gagal (bukan diam-diam null)
                val aiError      = if (aiSuggestion == null) aiResult.exceptionOrNull()?.message else null

                _uiState.update { current ->
                    if (current is ResultUiState.Success) {
                        current.copy(
                            analysis    = current.analysis.copy(
                                aiSuggestion = aiSuggestion ?: aiError?.let { "⚠️ $it" }
                            ),
                            isAiLoading = false
                        )
                    } else current
                }

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

            is ResultUiState.NoProfile -> NoProfileContent(
                modifier = Modifier.padding(padding),
                onGoToProfile = onNavigateToProfile
            )

            is ResultUiState.Error -> ErrorMessage(
                message  = state.message,
                modifier = Modifier.padding(padding),
                onRetry  = viewModel::retry
            )

            is ResultUiState.Success -> ResultContent(
                product     = state.product,
                profile     = state.profile,
                analysis    = state.analysis,
                isAiLoading = state.isAiLoading,
                modifier    = Modifier.padding(padding)
            )
        }
    }
}

// ── No Profile ────────────────────────────────────────────────────────────────

@Composable
private fun NoProfileContent(
    onGoToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxSize().padding(24.dp),
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
        Text("Profil diperlukan untuk analisis", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Lengkapi profil Anda agar NutriScan bisa memberikan analisis yang dipersonalisasi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onGoToProfile) {
            Text("Lengkapi Profil")
        }
    }
}

// ── Result Content ────────────────────────────────────────────────────────────

@Composable
private fun ResultContent(
    product: Product,
    profile: UserProfile,
    analysis: NutritionAnalysis,
    isAiLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Gambar produk ────────────────────────────────────────────────────
        if (product.imageUrl.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model             = product.imageUrl,
                    contentDescription = product.displayName,
                    contentScale      = ContentScale.Fit,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        // ── Header produk ────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.displayName, style = MaterialTheme.typography.titleLarge)
                if (product.brand.isNotBlank()) {
                    Text(
                        product.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Status keseluruhan ───────────────────────────────────────────────
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
                    Text(
                        "Status untuk ${profile.name}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "Per sajian ${product.servingSize.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = analysis.overallStatus)
            }
        }

        // ── Detail nutrisi ───────────────────────────────────────────────────
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

        // ── Peringatan ───────────────────────────────────────────────────────
        if (analysis.warningMessages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Peringatan",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    analysis.warningMessages.forEach { msg ->
                        Text(
                            "• $msg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // ── Saran AI ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        "Saran AI",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (isAiLoading) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color     = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Sedang menganalisis...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                } else {
                    Text(
                        text  = analysis.aiSuggestion ?: "Saran AI tidak tersedia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
