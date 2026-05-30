package com.example.nutriscan.presentation.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.core.util.format1
import com.example.nutriscan.domain.model.ConsumptionEntry
import com.example.nutriscan.domain.model.NutritionAnalysis
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.AIRepository
import com.example.nutriscan.domain.repository.ConsumptionRepository
import com.example.nutriscan.domain.repository.ProductRepository
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.domain.repository.UserProfileRepository
import com.example.nutriscan.domain.usecase.AnalyzeNutritionUseCase
import com.example.nutriscan.presentation.components.AlertType
import com.example.nutriscan.presentation.components.GradientButton
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.NutrientBar
import com.example.nutriscan.presentation.components.SoftCard
import com.example.nutriscan.presentation.components.StatusBadgeLarge
import com.example.nutriscan.presentation.components.SweetAlertDialog
import com.example.nutriscan.presentation.theme.AppGradients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

// ==================== UI STATE + VIEWMODEL ====================

/** Status of the AI advice request, shown below the analysis. */
sealed interface AiAdviceState {
    data object Idle : AiAdviceState
    data object Loading : AiAdviceState
    data class Success(val advice: String) : AiAdviceState
    data class Error(val message: String) : AiAdviceState
}

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(
        val product: Product,
        val profile: UserProfile,
        val analysis: NutritionAnalysis,
        val ai: AiAdviceState = AiAdviceState.Idle
    ) : ResultUiState
    data class NoProfile(val barcode: String) : ResultUiState
    data class Error(val message: String) : ResultUiState
}

class ResultViewModel(
    private val barcode: String,
    private val userProfileRepository: UserProfileRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val productRepository: ProductRepository,
    private val analyzeNutritionUseCase: AnalyzeNutritionUseCase,
    private val aiRepository: AIRepository,
    private val consumptionRepository: ConsumptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    /** One-shot signal: set to the logged label after a successful consume,
     *  consumed by the screen to show a success popup, then cleared. */
    private val _consumedLabel = MutableStateFlow<String?>(null)
    val consumedLabel: StateFlow<String?> = _consumedLabel.asStateFlow()

    init { loadResult() }

    private fun loadResult() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.getProfile().first()
                if (profile == null) {
                    _uiState.value = ResultUiState.NoProfile(barcode)
                    return@launch
                }

                // Use cached scan (offline-first) if we've seen this barcode before.
                val cached = scanHistoryRepository.getScanByBarcode(barcode)
                if (cached != null) {
                    _uiState.value = ResultUiState.Success(cached.product, profile, cached.analysis)
                    fetchAiAdvice(cached.product, profile, cached.analysis)
                    return@launch
                }

                // Fetch real product data from OpenFoodFacts.
                val product = productRepository.getProduct(barcode).getOrElse { e ->
                    _uiState.value = ResultUiState.Error(
                        e.message ?: "Gagal memuat produk. Periksa koneksi internet Anda."
                    )
                    return@launch
                }

                val analysis = analyzeNutritionUseCase(product, profile)
                _uiState.value = ResultUiState.Success(product, profile, analysis)

                // Persist to history (offline-first cache).
                runCatching {
                    scanHistoryRepository.saveScan(ScanResult(product = product, analysis = analysis))
                }

                fetchAiAdvice(product, profile, analysis)
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    private fun fetchAiAdvice(product: Product, profile: UserProfile, analysis: NutritionAnalysis) {
        setAi(AiAdviceState.Loading)
        viewModelScope.launch {
            val productSummary = buildString {
                val n = product.nutrimentsPerServing
                append("${product.displayName} (${product.brand.ifBlank { "tanpa merek" }}), ")
                append("per sajian ${product.servingSize.roundToInt()}g: ")
                append("${n.calories.roundToInt()} kkal, gula ${n.sugar.roundToInt()}g, ")
                append("garam ${n.sodium.roundToInt()}mg, lemak ${n.fat.roundToInt()}g, ")
                append("protein ${n.protein.roundToInt()}g, karbo ${n.carbs.roundToInt()}g")
            }
            val profileSummary = buildString {
                append("${profile.name}, usia ${profile.age}, BMI ${formatBmi(profile.bmi)} (${profile.bmiCategory})")
                if (profile.healthConditions.isNotEmpty()) {
                    append(", riwayat: ${profile.healthConditions.joinToString { it.displayName }}")
                }
            }
            val analysisSummary = buildString {
                append("Status keseluruhan: ${analysis.overallStatus.displayName}. ")
                if (analysis.warningMessages.isNotEmpty()) {
                    append("Catatan: ${analysis.warningMessages.joinToString("; ")}")
                } else {
                    append("Tidak ada peringatan nutrisi yang signifikan.")
                }
            }

            aiRepository.nutritionAdvice(productSummary, profileSummary, analysisSummary)
                .onSuccess { advice -> setAi(AiAdviceState.Success(advice)) }
                .onFailure { e ->
                    setAi(AiAdviceState.Error(e.message ?: "Saran AI tidak tersedia saat ini."))
                }
        }
    }

    private fun setAi(state: AiAdviceState) {
        _uiState.update { current ->
            if (current is ResultUiState.Success) current.copy(ai = state) else current
        }
    }

    fun retry() {
        _uiState.value = ResultUiState.Loading
        loadResult()
    }

    fun retryAi() {
        val current = _uiState.value
        if (current is ResultUiState.Success) {
            fetchAiAdvice(current.product, current.profile, current.analysis)
        }
    }

    /** Log a consumption entry to the daily log (drives Beranda totals). */
    fun consume(entry: ConsumptionEntry) {
        viewModelScope.launch {
            runCatching { consumptionRepository.add(entry) }
                .onSuccess { _consumedLabel.value = entry.amountLabel }
        }
    }

    fun clearConsumedSignal() { _consumedLabel.value = null }
}

private fun formatBmi(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

// ==================== SCREEN ====================

@Composable
fun ResultScreen(
    barcode: String,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(barcode) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val consumedLabel by viewModel.consumedLabel.collectAsStateWithLifecycle()

    // Popups shown over the screen content.
    var showConsumeDialog by remember { mutableStateOf(false) }
    var showStatusInfo by remember { mutableStateOf(false) }

    // Auto-show the status popup once the product is successfully loaded.
    val successState = uiState as? ResultUiState.Success
    LaunchedEffect(successState?.product?.barcode) {
        if (successState != null) showStatusInfo = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Hasil Scan",
            subtitle = "Analisis nutrisi produk",
            onBack = onNavigateBack
        )

        when (val state = uiState) {
            is ResultUiState.Loading -> LoadingIndicator()

            is ResultUiState.NoProfile -> NoProfileContent()

            // Error is surfaced as a SweetAlert popup (see below); keep a calm
            // placeholder behind it.
            is ResultUiState.Error -> Box(modifier = Modifier.fillMaxSize())

            is ResultUiState.Success -> ResultContent(
                product = state.product,
                profile = state.profile,
                analysis = state.analysis,
                ai = state.ai,
                onRetryAi = viewModel::retryAi,
                onConsumeClick = { showConsumeDialog = true }
            )
        }
    }

    // ── Error popup ──────────────────────────────────────────────────────────
    (uiState as? ResultUiState.Error)?.let { err ->
        SweetAlertDialog(
            type = AlertType.ERROR,
            title = "Produk Tidak Ditemukan",
            message = err.message,
            confirmText = "Coba Lagi",
            onConfirm = viewModel::retry,
            dismissText = "Kembali",
            onDismiss = onNavigateBack
        )
    }

    // ── Status popup (success / warning) on load ───────────────────────────────
    if (showStatusInfo && successState != null) {
        val analysis = successState.analysis
        val (type, title, message) = statusAlert(successState.product.displayName, analysis)
        SweetAlertDialog(
            type = type,
            title = title,
            message = message,
            confirmText = "Mengerti",
            onConfirm = { showStatusInfo = false }
        )
    }

    // ── Consumption dialog ─────────────────────────────────────────────────────
    if (showConsumeDialog && successState != null) {
        ConsumptionDialog(
            product = successState.product,
            onDismiss = { showConsumeDialog = false },
            onConfirm = { entry ->
                showConsumeDialog = false
                viewModel.consume(entry)
            }
        )
    }

    // ── Consumed success popup ──────────────────────────────────────────────────
    consumedLabel?.let { label ->
        SweetAlertDialog(
            type = AlertType.SUCCESS,
            title = "Tercatat!",
            message = "$label ditambahkan ke konsumsi harianmu. Lihat ringkasannya di Beranda.",
            confirmText = "Selesai",
            onConfirm = viewModel::clearConsumedSignal
        )
    }
}

/** Map an analysis result to the status popup's type/title/message. */
private fun statusAlert(
    productName: String,
    analysis: NutritionAnalysis
): Triple<AlertType, String, String> = when (analysis.overallStatus) {
    com.example.nutriscan.domain.model.NutritionStatus.SAFE -> Triple(
        AlertType.SUCCESS,
        "Aman Dikonsumsi",
        "$productName tergolong aman sesuai profil kesehatanmu. Tetap perhatikan porsinya ya!"
    )
    com.example.nutriscan.domain.model.NutritionStatus.CAUTION -> Triple(
        AlertType.WARNING,
        "Perlu Perhatian",
        analysis.warningMessages.firstOrNull()
            ?: "$productName perlu diperhatikan. Batasi porsinya agar tetap sesuai kebutuhan harianmu."
    )
    com.example.nutriscan.domain.model.NutritionStatus.AVOID -> Triple(
        AlertType.ERROR,
        "Sebaiknya Dihindari",
        analysis.warningMessages.firstOrNull()
            ?: "$productName sebaiknya dihindari sesuai kondisi kesehatanmu."
    )
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
    analysis: NutritionAnalysis,
    ai: AiAdviceState,
    onRetryAi: () -> Unit,
    onConsumeClick: () -> Unit
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
                        "Per sajian ${product.servingSize.format1()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadgeLarge(status = analysis.overallStatus)
            }
        }

        // Consume CTA — user decides the portion before it counts toward daily totals.
        GradientButton(
            text = "Catat Konsumsi",
            onClick = onConsumeClick,
            leadingIcon = Icons.Filled.Restaurant,
            modifier = Modifier.fillMaxWidth()
        )

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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Saran AI NutriScan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(6.dp))
                    when (ai) {
                        is AiAdviceState.Loading -> Text(
                            text = "Menyusun saran personal untukmu...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        is AiAdviceState.Success -> Text(
                            text = ai.advice,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                        is AiAdviceState.Error -> Column {
                            Text(
                                text = ai.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Coba lagi",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.clickable { onRetryAi() }
                            )
                        }
                        is AiAdviceState.Idle -> Text(
                            text = "Memuat saran...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
