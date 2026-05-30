package com.example.nutriscan.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.ConsumptionEntry
import com.example.nutriscan.domain.model.NutritionStatus
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.ConsumptionRepository
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import com.example.nutriscan.domain.repository.SessionRepository
import com.example.nutriscan.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

// ==================== UI STATE ====================

/** A day's calorie total for the weekly chart. */
data class DayCalories(val label: String, val calories: Float, val isToday: Boolean)

data class HomeDashboard(
    val userName: String = "",
    val profile: UserProfile? = null,
    val recentScans: List<ScanResult> = emptyList(),
    val totalScans: Int = 0,
    val todayCalories: Float = 0f,
    val todaySugar: Float = 0f,
    val todaySodium: Float = 0f,
    val todayFat: Float = 0f,
    val todayProtein: Float = 0f,
    val calorieTarget: Float = 2000f,
    val weekly: List<DayCalories> = emptyList(),
    val todayEntries: List<ConsumptionEntry> = emptyList(),
    val safeCount: Int = 0,
    val cautionCount: Int = 0,
    val avoidCount: Int = 0
) {
    val calorieProgress: Float
        get() = if (calorieTarget > 0f) (todayCalories / calorieTarget) else 0f
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val dashboard: HomeDashboard) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

// ==================== VIEWMODEL ====================

class HomeViewModel(
    private val scanHistoryRepository: ScanHistoryRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sessionRepository: SessionRepository,
    private val consumptionRepository: ConsumptionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        scanHistoryRepository.getAllHistory(),
        userProfileRepository.getProfile(),
        sessionRepository.state,
        consumptionRepository.observeAll()
    ) { scans, profile, session, consumption ->
        HomeUiState.Ready(buildDashboard(scans, profile, session.userName, consumption)) as HomeUiState
    }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Terjadi kesalahan")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    private fun buildDashboard(
        scans: List<ScanResult>,
        profile: UserProfile?,
        userName: String,
        consumption: List<ConsumptionEntry>
    ): HomeDashboard {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)

        fun dateOf(epoch: Long): LocalDate =
            Instant.fromEpochMilliseconds(epoch).toLocalDateTime(tz).date

        // ── Today's consumed nutrition (sum over today's consumption entries) ──
        val todayEntries = consumption.filter { it.consumedAt != 0L && dateOf(it.consumedAt) == today }
        var cal = 0f; var sugar = 0f; var sodium = 0f; var fat = 0f; var protein = 0f
        todayEntries.forEach {
            cal += it.calories; sugar += it.sugar; sodium += it.sodium; fat += it.fat; protein += it.protein
        }

        // ── Weekly calories (oldest -> newest) from consumption ──
        val weekly = (6 downTo 0).map { offset ->
            val day = today.minus(offset, DateTimeUnit.DAY)
            val dayCalories = consumption
                .filter { it.consumedAt != 0L && dateOf(it.consumedAt) == day }
                .sumOf { it.calories.toDouble() }
                .toFloat()
            DayCalories(label = day.dayOfWeek.shortId(), calories = dayCalories, isToday = offset == 0)
        }

        return HomeDashboard(
            userName = profile?.name?.takeIf { it.isNotBlank() }
                ?: userName.ifBlank { "Pengguna" },
            profile = profile,
            recentScans = scans.take(5),
            totalScans = scans.size,
            todayCalories = cal,
            todaySugar = sugar,
            todaySodium = sodium,
            todayFat = fat,
            todayProtein = protein,
            calorieTarget = profile?.dailyCalorieNeed?.takeIf { it > 0f } ?: 2000f,
            weekly = weekly,
            todayEntries = todayEntries,
            safeCount = scans.count { it.analysis.overallStatus == NutritionStatus.SAFE },
            cautionCount = scans.count { it.analysis.overallStatus == NutritionStatus.CAUTION },
            avoidCount = scans.count { it.analysis.overallStatus == NutritionStatus.AVOID }
        )
    }
}

private fun DayOfWeek.shortId(): String = when (this) {
    DayOfWeek.MONDAY -> "Sen"
    DayOfWeek.TUESDAY -> "Sel"
    DayOfWeek.WEDNESDAY -> "Rab"
    DayOfWeek.THURSDAY -> "Kam"
    DayOfWeek.FRIDAY -> "Jum"
    DayOfWeek.SATURDAY -> "Sab"
    DayOfWeek.SUNDAY -> "Min"
    else -> "?"
}
