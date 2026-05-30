package com.example.nutriscan.presentation.screens.result

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.NutrientBar
import com.example.nutriscan.presentation.components.SoftCard
import com.example.nutriscan.presentation.components.StatusBadgeLarge
import com.example.nutriscan.presentation.theme.AppGradients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

// ==================== UI STATE + VIEWMODEL ====================

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(
        val product: Product,
        val profile: UserProfile,
        val analysis: NutritionAnalysis
    ) : ResultUiState
    data class NoProfile(val barcode: String) : ResultUiState
    data class Error(val message: String) : ResultUiState
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
                val profile = userProfileRepository.getProfile().first()
                if (profile == null) {
                    _uiState.value = ResultUiState.NoProfile(barcode)
                    return@launch
                }

                val cached = scanHistoryRepository.getScanById(barcode.toLongOrNull() ?: -1L)
                if (cached != null) {
                    _uiState.value = ResultUiState.Success(cached.product, profile, cached.analysis)
                    return@launch
                }

                val product = Product(
                    barcode = barcode,
                    name = "Memuat produk...",
                    brand = "",
                    imageUrl = ""
                )
                val analysis = analyzeNutritionUseCase(product, profile)
                _uiState.value = ResultUiState.Success(product, profile, analysis)
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

@Composable
fun ResultScreen(
    barcode: String,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(barcode) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Hasil Scan",
            subtitle = "Analisis nutrisi produk",
            onBack = onNavigateBack
        )

        when (val state = uiState) {
            is ResultUiState.Loading -> LoadingIndicator()

            is ResultUiState.NoProfile -> NoProfileContent()

            is ResultUiState.Error -> ErrorMessage(message = state.message, onRetry = viewModel::retry)

            is ResultUiState.Success -> ResultContent(
                product = state.product,
                profile = state.profile,
                analysis = state.analysis
            )
        }
    }
}

@Composable
private fun NoProfileContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Profil diperlukan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Lengkapi profil agar NutriScan bisa memberi analisis yang dipersonalisasi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultContent(
    product: Product,
    profile: UserProfile,
    analysis: NutritionAnalysis
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Product header
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Fastfood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (product.brand.isNotBlank()) {
                        Text(product.brand, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "Barcode: ${product.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Overall status
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Status untuk ${profile.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Per sajian ${product.servingSize.roundToInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadgeLarge(status = analysis.overallStatus)
            }
        }

        // Nutrient bars
        if (analysis.warnings.isNotEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Text("Detail Nutrisi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    analysis.warnings.forEach { w ->
                        NutrientBar(
                            label = w.nutrientName,
                            value = w.valuePerServing,
                            unit = w.unit,
                            percentDaily = w.percentDailyValue,
                            status = w.status
                        )
                    }
                }
            }
        }

        // Warnings
        if (analysis.warningMessages.isNotEmpty()) {
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Peringatan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
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

        // AI suggestion / info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppGradients.brand)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("Saran NutriScan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = analysis.aiSuggestion
                            ?: "Data nutrisi lengkap & saran AI akan tampil setelah integrasi OpenFoodFacts & Gemini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
