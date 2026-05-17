package com.example.nutriscan.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriscan.domain.model.NutritionStatus

@Composable
fun StatusChip(
    status: NutritionStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        NutritionStatus.SAFE    -> Color(0xFF43A047) to Color.White
        NutritionStatus.CAUTION -> Color(0xFFFFA726) to Color.White
        NutritionStatus.AVOID   -> MaterialTheme.colorScheme.error to Color.White
    }

    Surface(
        modifier  = modifier,
        shape     = MaterialTheme.shapes.small,
        color     = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Text(
            text       = "${status.emoji} ${status.displayName}",
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = contentColor
        )
    }
}
