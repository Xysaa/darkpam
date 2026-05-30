package com.example.nutriscan.presentation.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.presentation.components.BarEntry
import com.example.nutriscan.presentation.components.EmptyState
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.RingProgress
import com.example.nutriscan.presentation.components.ScanResultCard
import com.example.nutriscan.presentation.components.SectionHeader
import com.example.nutriscan.presentation.components.SoftCard
import com.example.nutriscan.presentation.components.StatusBreakdownBar
import com.example.nutriscan.presentation.components.StatusSlice
import com.example.nutriscan.presentation.components.WeeklyBarChart
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.NutrientFat
import com.example.nutriscan.presentation.theme.NutrientSodium
import com.example.nutriscan.presentation.theme.NutrientSugar
import com.example.nutriscan.presentation.theme.StatusAvoid
import com.example.nutriscan.presentation.theme.StatusCaution
import com.example.nutriscan.presentation.theme.StatusSafe
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onOpenResult: (String) -> Unit,
    onSeeAllHistory: () -> Unit,
    onOpenConsultation: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                GradientHeader(title = "Halo 👋", subtitle = "Memuat ringkasan nutrisimu...")
                LoadingIndicator()
            }

            is HomeUiState.Error -> {
                GradientHeader(title = "Beranda")
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "Gagal memuat",
                    subtitle = state.message,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is HomeUiState.Ready -> DashboardContent(
                dashboard = state.dashboard,
                onScanClick = onScanClick,
                onOpenResult = onOpenResult,
                onSeeAllHistory = onSeeAllHistory,
                onOpenConsultation = onOpenConsultation
            )
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: HomeDashboard,
    onScanClick: () -> Unit,
    onOpenResult: (String) -> Unit,
    onSeeAllHistory: () -> Unit,
    onOpenConsultation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Halo, ${dashboard.userName} 👋",
            subtitle = "Yuk pantau nutrisimu hari ini"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Calorie ring + macros ───────────────────────────────────────
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Konsumsi Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RingProgress(
                        progress = dashboard.calorieProgress,
                        diameter = 132.dp,
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${dashboard.todayCalories.roundToInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "/ ${dashboard.calorieTarget.roundToInt()} kkal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.size(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroStat("Gula", dashboard.todaySugar, "g", NutrientSugar)
                        MacroStat("Garam", dashboard.todaySodium, "mg", NutrientSodium)
                        MacroStat("Lemak", dashboard.todayFat, "g", NutrientFat)
                    }
                }
            }

            // ── Consultation promo ──────────────────────────────────────────
            ConsultationPromo(onClick = onOpenConsultation)

            // ── Weekly activity ─────────────────────────────────────────────
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Aktivitas 7 Hari")
                Spacer(Modifier.height(16.dp))
                WeeklyBarChart(
                    data = dashboard.weekly.map {
                        BarEntry(label = it.label, value = it.count.toFloat(), highlighted = it.isToday)
                    }
                )
            }

            // ── Status breakdown ────────────────────────────────────────────
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Ringkasan Status Produk")
                Spacer(Modifier.height(14.dp))
                if (dashboard.totalScans == 0) {
                    Text(
                        "Belum ada produk yang dipindai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    StatusBreakdownBar(
                        slices = listOf(
                            StatusSlice("Aman", dashboard.safeCount, StatusSafe),
                            StatusSlice("Perhatian", dashboard.cautionCount, StatusCaution),
                            StatusSlice("Hindari", dashboard.avoidCount, StatusAvoid)
                        )
                    )
                }
            }

            // ── Recent scans ────────────────────────────────────────────────
            SectionHeader(
                title = "Scan Terakhir",
                actionText = if (dashboard.recentScans.isNotEmpty()) "Lihat semua" else null,
                onActionClick = onSeeAllHistory.takeIf { dashboard.recentScans.isNotEmpty() }
            )

            if (dashboard.recentScans.isEmpty()) {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Belum ada riwayat. Tekan tombol scan untuk memulai!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                dashboard.recentScans.forEach { scan ->
                    ScanResultCard(
                        scan = scan,
                        onClick = { onOpenResult(scan.product.barcode) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MacroStat(label: String, value: Float, unit: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.size(8.dp))
        Column {
            Text(
                text = "${formatNumber(value)} $unit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConsultationPromo(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppGradients.energy)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Forum, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Konsultasi dengan Ahli Gizi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Dapatkan saran nutrisi yang dipersonalisasi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

private fun formatNumber(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
