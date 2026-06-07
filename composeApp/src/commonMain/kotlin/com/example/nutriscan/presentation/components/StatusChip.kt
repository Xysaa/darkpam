package com.example.nutriscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriscan.domain.model.NutritionStatus
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.StatusAvoid
import com.example.nutriscan.presentation.theme.StatusCaution
import com.example.nutriscan.presentation.theme.StatusSafe

private fun NutritionStatus.color(): Color = when (this) {
    NutritionStatus.SAFE    -> StatusSafe
    NutritionStatus.CAUTION -> StatusCaution
    NutritionStatus.AVOID   -> StatusAvoid
}

private fun NutritionStatus.brush(): Brush = when (this) {
    NutritionStatus.SAFE    -> AppGradients.safe
    NutritionStatus.CAUTION -> AppGradients.caution
    NutritionStatus.AVOID   -> AppGradients.avoid
}

private fun NutritionStatus.icon(): ImageVector = when (this) {
    NutritionStatus.SAFE    -> Icons.Filled.CheckCircle
    NutritionStatus.CAUTION -> Icons.Filled.WarningAmber
    NutritionStatus.AVOID   -> Icons.Filled.Error
}

/** Compact status pill used in list rows. */
@Composable
fun StatusChip(
    status: NutritionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(status.color().copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = status.icon(),
            contentDescription = null,
            tint = status.color(),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = status.color()
        )
    }
}

/** Large gradient status banner used on the Result screen. */
@Composable
fun StatusBadgeLarge(
    status: NutritionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(status.brush())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = status.icon(),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
