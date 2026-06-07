package com.example.nutriscan.presentation.screens.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.Conversation
import com.example.nutriscan.domain.repository.ConsultationRepository
import com.example.nutriscan.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class NutritionistDashboardUiState(
    val nutritionistName: String = "",
    val conversations: List<Conversation> = emptyList()
)

class NutritionistDashboardViewModel(
    private val consultationRepository: ConsultationRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<NutritionistDashboardUiState> = combine(
        sessionRepository.state,
        consultationRepository.observeConversations()
    ) { session, conversations ->
        NutritionistDashboardUiState(
            nutritionistName = session.userName.ifBlank { "Ahli Gizi" },
            conversations = conversations
        )
    }.catch {
        emit(NutritionistDashboardUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionistDashboardUiState()
    )
}
