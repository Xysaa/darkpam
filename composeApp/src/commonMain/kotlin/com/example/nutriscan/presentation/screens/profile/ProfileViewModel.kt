package com.example.nutriscan.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.usecase.GetUserProfileUseCase
import com.example.nutriscan.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==================== UI STATE ====================

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class  Viewing(val profile: UserProfile) : ProfileUiState
    data class  Editing(val profile: UserProfile, val form: ProfileFormState) : ProfileUiState
    data class  Saving(val profile: UserProfile) : ProfileUiState
    data class  Error(val message: String) : ProfileUiState
}

data class ProfileFormState(
    val name: String               = "",
    val age: String                = "",
    val weight: String             = "",
    val height: String             = "",
    val selectedDiseases: Set<Disease> = emptySet(),
    val nameError: String?         = null,
    val ageError: String?          = null,
    val weightError: String?       = null,
    val heightError: String?       = null
) {
    val isValid: Boolean
        get() = name.isNotBlank()
            && age.toIntOrNull() != null
            && weight.toFloatOrNull() != null
            && height.toFloatOrNull() != null
            && nameError == null && ageError == null
            && weightError == null && heightError == null
}

// ==================== VIEWMODEL ====================

class ProfileViewModel(
    private val getProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProfileUseCase().collect { profile ->
                if (profile != null) {
                    // Hanya update dari flow DB kalau sedang tidak dalam proses edit/save aktif
                    if (_uiState.value !is ProfileUiState.Editing) {
                        _uiState.value = ProfileUiState.Viewing(profile)
                    }
                } else {
                    if (_uiState.value !is ProfileUiState.Editing) {
                        _uiState.value = ProfileUiState.Error("Profil tidak ditemukan")
                    }
                }
            }
        }
    }

    fun startEditing() {
        val current = (_uiState.value as? ProfileUiState.Viewing)?.profile ?: return
        _uiState.value = ProfileUiState.Editing(
            profile = current,
            form    = ProfileFormState(
                name             = current.name,
                age              = current.age.toString(),
                weight           = current.weight.toString(),
                height           = current.height.toString(),
                selectedDiseases = current.healthConditions.toSet()
            )
        )
    }

    fun cancelEditing() {
        val current = (_uiState.value as? ProfileUiState.Editing)?.profile ?: return
        _uiState.value = ProfileUiState.Viewing(current)
    }

    // ── Form field updaters ──────────────────────────────────────────────────

    fun onNameChange(value: String) = updateForm {
        copy(name = value, nameError = if (value.isBlank()) "Nama tidak boleh kosong" else null)
    }

    fun onAgeChange(value: String) = updateForm {
        copy(age = value, ageError = when {
            value.isBlank()             -> "Usia tidak boleh kosong"
            value.toIntOrNull() == null -> "Usia harus berupa angka"
            value.toInt() !in 1..120   -> "Usia harus antara 1–120 tahun"
            else                        -> null
        })
    }

    fun onWeightChange(value: String) = updateForm {
        copy(weight = value, weightError = when {
            value.isBlank()               -> "Berat badan tidak boleh kosong"
            value.toFloatOrNull() == null -> "Berat badan harus berupa angka"
            value.toFloat() !in 1f..500f  -> "Berat badan tidak valid"
            else                           -> null
        })
    }

    fun onHeightChange(value: String) = updateForm {
        copy(height = value, heightError = when {
            value.isBlank()               -> "Tinggi badan tidak boleh kosong"
            value.toFloatOrNull() == null -> "Tinggi badan harus berupa angka"
            value.toFloat() !in 50f..300f -> "Tinggi badan tidak valid"
            else                           -> null
        })
    }

    fun onDiseaseToggled(disease: Disease) = updateForm {
        val updated = if (disease in selectedDiseases) selectedDiseases - disease
                      else selectedDiseases + disease
        copy(selectedDiseases = updated)
    }

    fun saveProfile() {
        val editingState = _uiState.value as? ProfileUiState.Editing ?: return
        val f = editingState.form
        if (!f.isValid) return

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Saving(editingState.profile)

            val updated = editingState.profile.copy(
                name             = f.name.trim(),
                age              = f.age.toInt(),
                weight           = f.weight.toFloat(),
                height           = f.height.toFloat(),
                healthConditions = f.selectedDiseases.toList()
            )

            updateProfileUseCase(updated)
                .onSuccess  {
                    // Langsung pindah ke Viewing dengan data terbaru,
                    // tidak perlu menunggu flow DB — flow guard di init
                    // memblokir update saat state masih Saving.
                    _uiState.value = ProfileUiState.Viewing(updated)
                }
                .onFailure  { e ->
                    _uiState.value = ProfileUiState.Error(e.message ?: "Gagal menyimpan profil")
                }
        }
    }

    private fun updateForm(block: ProfileFormState.() -> ProfileFormState) {
        val state = _uiState.value as? ProfileUiState.Editing ?: return
        _uiState.value = state.copy(form = state.form.block())
    }
}
