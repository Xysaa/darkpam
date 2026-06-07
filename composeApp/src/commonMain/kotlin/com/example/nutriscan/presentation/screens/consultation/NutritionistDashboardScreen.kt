package com.example.nutriscan.presentation.screens.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.presentation.components.EmptyState
import com.example.nutriscan.presentation.components.GradientHeader
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NutritionistDashboardScreen(
    onOpenChat: (Long) -> Unit,
    viewModel: NutritionistDashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Konsultasi Masuk",
            subtitle = "Halo, ${state.nutritionistName}"
        )

        if (state.conversations.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Forum,
                title = "Belum ada konsultasi",
                subtitle = "Percakapan dari pengguna akan muncul di sini.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "${state.conversations.size} percakapan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 14.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.conversations, key = { it.id }) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        isNutritionistView = true,
                        onClick = { onOpenChat(conversation.id) }
                    )
                }
            }
        }
    }
}
