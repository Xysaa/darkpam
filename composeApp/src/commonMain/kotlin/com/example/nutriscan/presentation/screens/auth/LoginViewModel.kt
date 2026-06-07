package com.example.nutriscan.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginFormState(
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val submitting: Boolean = false
)

class LoginViewModel(
    private val session: SessionRepository
) : ViewModel() {

    private val _form = MutableStateFlow(LoginFormState())
    val form: StateFlow<LoginFormState> = _form.asStateFlow()

    fun onNameChange(value: String) = _form.update { it.copy(name = value) }

    fun selectRole(role: UserRole) = _form.update { it.copy(role = role) }

    fun login() {
        val current = _form.value
        if (current.submitting) return
        _form.update { it.copy(submitting = true) }

        viewModelScope.launch {
            val fallback = if (current.role == UserRole.NUTRITIONIST) "Ahli Gizi" else "Pengguna"
            val finalName = current.name.trim().ifBlank { fallback }
            // The reactive session state in App.kt handles the navigation switch.
            session.login(current.role, finalName)
        }
    }
}
