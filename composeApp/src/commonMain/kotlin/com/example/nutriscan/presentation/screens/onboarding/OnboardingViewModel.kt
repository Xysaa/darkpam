package com.example.nutriscan.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.usecase.SaveUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==================== UI STATE ====================

sealed interface OnboardingUiState {
    data object Idle    : OnboardingUiState
    data object Loading : OnboardingUiState
    data object Success : OnboardingUiState
    data class  Error(val message: String) : OnboardingUiState
}

data class OnboardingFormState(
    val name: String                    = "",
    val age: String                     = "",
    val weight: String                  = "",
    val height: String                  = "",
    val selectedDiseases: Set<Disease>  = emptySet(),
    // Validation errors
    val nameError: String?    = null,
    val ageError: String?     = null,
    val weightError: String?  = null,
    val heightError: String?  = null
) {
    val isValid: Boolean
        get() = name.isNotBlank()
            && age.toIntOrNull() != null
            && weight.toFloatOrNull() != null
            && height.toFloatOrNull() != null
            && nameError == null
            && ageError == null
            && weightError == null
            && heightError == null
}

// ==================== VIEWMODEL ====================

class OnboardingViewModel(
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(OnboardingFormState())
    val form: StateFlow<OnboardingFormState> = _form.asStateFlow()

    // ── Form field updaters ──────────────────────────────────────────────────

    fun onNameChange(value: String) {
        _form.update {
            it.copy(
                name      = value,
                nameError = if (value.isBlank()) "Nama tidak boleh kosong" else null
            )
        }
    }

    fun onAgeChange(value: String) {
        _form.update {
            it.copy(
                age      = value,
                ageError = when {
                    value.isBlank()                   -> "Usia tidak boleh kosong"
                    value.toIntOrNull() == null       -> "Usia harus berupa angka"
                    value.toInt() !in 1..120          -> "Usia harus antara 1–120 tahun"
                    else                              -> null
                }
            )
        }
    }

    fun onWeightChange(value: String) {
        _form.update {
            it.copy(
                weight      = value,
                weightError = when {
                    value.isBlank()                          -> "Berat badan tidak boleh kosong"
                    value.toFloatOrNull() == null            -> "Berat badan harus berupa angka"
                    value.toFloat() !in 1f..500f             -> "Berat badan tidak valid"
                    else                                     -> null
                }
            )
        }
    }

    fun onHeightChange(value: String) {
        _form.update {
            it.copy(
                height      = value,
                heightError = when {
                    value.isBlank()                          -> "Tinggi badan tidak boleh kosong"
                    value.toFloatOrNull() == null            -> "Tinggi badan harus berupa angka"
                    value.toFloat() !in 50f..300f            -> "Tinggi badan tidak valid"
                    else                                     -> null
                }
            )
        }
    }

    fun onDiseaseToggled(disease: Disease) {
        _form.update {
            val updated = if (disease in it.selectedDiseases) {
                it.selectedDiseases - disease
            } else {
                it.selectedDiseases + disease
            }
            it.copy(selectedDiseases = updated)
        }
    }

    // ── Submit ───────────────────────────────────────────────────────────────

    fun saveProfile() {
        val f = _form.value
        if (!f.isValid) return

        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading

            val profile = UserProfile(
                name             = f.name.trim(),
                age              = f.age.toInt(),
                weight           = f.weight.toFloat(),
                height           = f.height.toFloat(),
                healthConditions = f.selectedDiseases.toList()
            )

            saveUserProfileUseCase(profile)
                .onSuccess {
                    userPreferences.setOnboardingCompleted()
                    _uiState.value = OnboardingUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = OnboardingUiState.Error(e.message ?: "Gagal menyimpan profil")
                }
        }
    }

    fun resetError() {
        if (_uiState.value is OnboardingUiState.Error) {
            _uiState.value = OnboardingUiState.Idle
        }
    }
}
