package com.example.nutriscan.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.local.datastore.UserPreferences
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.SessionRepository
import com.example.nutriscan.domain.usecase.GetUserProfileUseCase
import com.example.nutriscan.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==================== UI STATE ====================

data class ProfileFormState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val selectedDiseases: Set<Disease> = emptySet(),
    val nameError: String? = null,
    val ageError: String? = null,
    val weightError: String? = null,
    val heightError: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull() != null &&
            weight.toFloatOrNull() != null &&
            height.toFloatOrNull() != null &&
            nameError == null && ageError == null &&
            weightError == null && heightError == null
}

data class ProfileUiState(
    val loading: Boolean = true,
    val role: UserRole = UserRole.USER,
    val userName: String = "",
    val coins: Int = 0,
    val darkMode: Boolean = false,
    val profile: UserProfile? = null,
    val editForm: ProfileFormState? = null,
    val message: String? = null
)

// ==================== VIEWMODEL ====================

class ProfileViewModel(
    private val getProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateUserProfileUseCase,
    private val sessionRepository: SessionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val editForm = MutableStateFlow<ProfileFormState?>(null)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        sessionRepository.state,
        userPreferences.isDarkMode,
        getProfileUseCase(),
        editForm,
        message
    ) { session, dark, profile, form, msg ->
        ProfileUiState(
            loading = false,
            role = session.role,
            userName = session.userName,
            coins = session.coins,
            darkMode = dark,
            profile = profile,
            editForm = form,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    // ── Edit lifecycle ──
    fun startEditing() {
        val current = uiState.value.profile ?: return
        editForm.value = ProfileFormState(
            name = current.name,
            age = current.age.toString(),
            weight = current.weight.toString(),
            height = current.height.toString(),
            selectedDiseases = current.healthConditions.toSet()
        )
    }

    fun cancelEditing() { editForm.value = null }

    fun onNameChange(value: String) = updateForm {
        copy(name = value, nameError = if (value.isBlank()) "Nama tidak boleh kosong" else null)
    }

    fun onAgeChange(value: String) = updateForm {
        copy(age = value, ageError = when {
            value.isBlank() -> "Usia tidak boleh kosong"
            value.toIntOrNull() == null -> "Usia harus berupa angka"
            value.toInt() !in 1..120 -> "Usia harus antara 1–120 tahun"
            else -> null
        })
    }

    fun onWeightChange(value: String) = updateForm {
        copy(weight = value, weightError = when {
            value.isBlank() -> "Berat badan tidak boleh kosong"
            value.toFloatOrNull() == null -> "Berat badan harus berupa angka"
            value.toFloat() !in 1f..500f -> "Berat badan tidak valid"
            else -> null
        })
    }

    fun onHeightChange(value: String) = updateForm {
        copy(height = value, heightError = when {
            value.isBlank() -> "Tinggi badan tidak boleh kosong"
            value.toFloatOrNull() == null -> "Tinggi badan harus berupa angka"
            value.toFloat() !in 50f..300f -> "Tinggi badan tidak valid"
            else -> null
        })
    }

    fun onDiseaseToggled(disease: Disease) = updateForm {
        val updated = if (disease in selectedDiseases) selectedDiseases - disease
        else selectedDiseases + disease
        copy(selectedDiseases = updated)
    }

    fun saveProfile() {
        val form = editForm.value ?: return
        val base = uiState.value.profile ?: return
        if (!form.isValid) return

        viewModelScope.launch {
            val updated = base.copy(
                name = form.name.trim(),
                age = form.age.toInt(),
                weight = form.weight.toFloat(),
                height = form.height.toFloat(),
                healthConditions = form.selectedDiseases.toList()
            )
            updateProfileUseCase(updated)
                .onSuccess {
                    editForm.value = null
                    message.value = "Profil berhasil diperbarui"
                }
                .onFailure { e -> message.value = e.message ?: "Gagal menyimpan profil" }
        }
    }

    // ── Settings / session ──
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setDarkMode(enabled) }
    }

    fun topUp(amount: Int = 50) {
        viewModelScope.launch {
            sessionRepository.topUp(amount)
            message.value = "+$amount coin ditambahkan"
        }
    }

    fun logout() {
        viewModelScope.launch { sessionRepository.logout() }
    }

    fun consumeMessage() { message.value = null }

    private fun updateForm(block: ProfileFormState.() -> ProfileFormState) {
        editForm.value = editForm.value?.block()
    }
}
