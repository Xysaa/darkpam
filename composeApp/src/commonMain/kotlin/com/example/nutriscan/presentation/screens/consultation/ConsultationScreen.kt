package com.example.nutriscan.presentation.screens.consultation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.presentation.components.CoinBadge
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.SectionHeader
import com.example.nutriscan.presentation.components.SoftCard
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConsultationScreen(
    onOpenChat: (Long) -> Unit,
    viewModel: ConsultationViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            delay(3500)
            viewModel.consumeError()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Konsultasi",
            subtitle = "Tanya langsung ke ahli gizi",
            actions = { CoinBadge(coins = state.coins) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(visible = state.errorMessage != null) {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (state.conversations.isNotEmpty()) {
                SectionHeader(title = "Konsultasi Aktif")
                state.conversations.forEach { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        isNutritionistView = false,
                        onClick = { onOpenChat(conversation.id) }
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            SectionHeader(title = "Pilih Ahli Gizi")
            Text(
                "Setiap konsultasi memerlukan coin sesuai tarif ahli gizi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.nutritionists.forEach { nutritionist ->
                NutritionistCard(
                    nutritionist = nutritionist,
                    onChat = { viewModel.startConsultation(nutritionist, onOpenChat) }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
