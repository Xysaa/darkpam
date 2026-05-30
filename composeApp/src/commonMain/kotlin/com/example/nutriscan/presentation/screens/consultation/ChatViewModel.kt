package com.example.nutriscan.presentation.screens.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.model.ChatMessage
import com.example.nutriscan.domain.model.Conversation
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.domain.repository.ConsultationRepository
import com.example.nutriscan.domain.repository.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val myRole: UserRole = UserRole.USER,
    val partnerTyping: Boolean = false
) {
    /** Title shown in the header = the other participant. */
    val partnerName: String
        get() = if (myRole == UserRole.USER) conversation?.nutritionistName ?: "Ahli Gizi"
        else conversation?.userName ?: "Pengguna"

    val partnerSubtitle: String
        get() = if (myRole == UserRole.USER) conversation?.nutritionistSpecialty ?: ""
        else "Pengguna"
}

class ChatViewModel(
    private val conversationId: Long,
    private val consultationRepository: ConsultationRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val conversation = MutableStateFlow<Conversation?>(null)
    private val typing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            conversation.value = consultationRepository.getConversation(conversationId)
        }
    }

    val uiState: StateFlow<ChatUiState> = combine(
        consultationRepository.observeMessages(conversationId),
        sessionRepository.state,
        typing,
        conversation
    ) { messages, session, isTyping, conv ->
        ChatUiState(
            conversation = conv,
            messages = messages,
            myRole = session.role,
            partnerTyping = isTyping
        )
    }.catch {
        emit(ChatUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState()
    )

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val myRole = uiState.value.myRole
        val partner = if (myRole == UserRole.USER) UserRole.NUTRITIONIST else UserRole.USER

        viewModelScope.launch {
            consultationRepository.sendMessage(conversationId, myRole, trimmed)
            // Simulated reply from the other side (dummy local behaviour).
            typing.value = true
            delay(1300)
            consultationRepository.sendMessage(conversationId, partner, cannedReply(trimmed, partner))
            typing.value = false
        }
    }

    private fun cannedReply(userText: String, partner: UserRole): String {
        if (partner == UserRole.USER) {
            return listOf(
                "Baik, terima kasih sarannya 🙏",
                "Oke, akan saya coba terapkan.",
                "Wah, informatif sekali. Terima kasih!"
            ).random()
        }
        val lower = userText.lowercase()
        return when {
            "gula" in lower || "manis" in lower || "diabetes" in lower ->
                "Untuk asupan gula, sebaiknya batasi < 25g per hari. Pilih produk dengan gula rendah dan perbanyak serat ya."
            "garam" in lower || "natrium" in lower || "hipertensi" in lower ->
                "Kurangi makanan tinggi natrium. Targetkan < 2000mg natrium/hari, dan perbanyak konsumsi sayur."
            "diet" in lower || "berat" in lower || "kurus" in lower || "gemuk" in lower ->
                "Untuk berat badan ideal, fokus pada defisit/surplus kalori yang sehat dan rutin bergerak. Boleh ceritakan target Anda?"
            "halo" in lower || "hai" in lower || "selamat" in lower ->
                "Halo juga! Senang bisa membantu. Ada keluhan atau target nutrisi yang ingin dikonsultasikan?"
            else ->
                "Terima kasih informasinya. Bisa ceritakan lebih detail pola makan harian Anda agar saya bisa beri saran yang tepat?"
        }
    }
}
