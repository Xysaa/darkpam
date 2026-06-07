package com.example.nutriscan.presentation.screens.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutriscan.domain.model.Conversation
import com.example.nutriscan.domain.model.Nutritionist
import com.example.nutriscan.presentation.components.GradientButton
import com.example.nutriscan.presentation.components.InitialsAvatar
import com.example.nutriscan.presentation.components.Pill
import com.example.nutriscan.presentation.components.SoftCard
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.CoinGoldDark
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** A nutritionist profile card with rating, experience and a Chat CTA. */
@Composable
fun NutritionistCard(
    nutritionist: Nutritionist,
    onChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            InitialsAvatar(
                initials = nutritionist.initials,
                size = 56.dp,
                brush = AppGradients.brand,
                online = nutritionist.isOnline
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nutritionist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    nutritionist.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = CoinGoldDark, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${nutritionist.rating} (${nutritionist.reviewCount})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${nutritionist.experienceYears} thn",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            nutritionist.bio,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(
                text = "${nutritionist.pricePerChat} coin",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            GradientButton(
                text = "Konsultasi",
                onClick = onChat,
                leadingIcon = Icons.Filled.Forum,
                height = 44.dp,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

/** A conversation row used in both the user's list and the nutritionist dashboard. */
@Composable
fun ConversationCard(
    conversation: Conversation,
    isNutritionistView: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val partnerName = if (isNutritionistView) conversation.userName else conversation.nutritionistName
    val initials = partnerName.firstOrNull()?.uppercase() ?: "?"

    SoftCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(
                initials = initials,
                size = 50.dp,
                brush = if (isNutritionistView) AppGradients.energy else AppGradients.brand
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        partnerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.lastMessageAt > 0L) {
                        Text(
                            formatClock(conversation.lastMessageAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!isNutritionistView) {
                    Text(
                        conversation.nutritionistSpecialty,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    conversation.lastMessage.ifBlank { "Mulai percakapan..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatClock(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}
