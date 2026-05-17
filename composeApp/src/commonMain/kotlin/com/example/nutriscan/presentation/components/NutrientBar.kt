package com.example.nutriscan.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutriscan.domain.model.NutritionStatus

/**
 * Animated progress bar for a single nutrient.
 *
 * @param label          Nutrient name, e.g. "Gula"
 * @param value          Amount in serving (g or mg)
 * @param unit           Unit string, e.g. "g" or "mg"
 * @param percentDaily   Percentage of daily recommended value (0–100+)
 * @param status         Drives the bar colour (SAFE/CAUTION/AVOID)
 */
@Composable
fun NutrientBar(
    label: String,
    value: Float,
    unit: String,
    percentDaily: Float,
    status: NutritionStatus,
    modifier: Modifier = Modifier
) {
    val barColor = when (status) {
        NutritionStatus.SAFE    -> MaterialTheme.colorScheme.primary
        NutritionStatus.CAUTION -> Color(0xFFFFA726)   // Orange
        NutritionStatus.AVOID   -> MaterialTheme.colorScheme.error
    }

    // Animate from 0 → actual progress on first composition
    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(percentDaily) { targetProgress = (percentDaily / 100f).coerceIn(0f, 1f) }
    val animatedProgress by animateFloatAsState(
        targetValue    = targetProgress,
        animationSpec  = tween(durationMillis = 600),
        label          = "nutrientBarProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text  = "${value.formatted()} $unit  (${percentDaily.toInt()}% AKG)",
                style = MaterialTheme.typography.bodySmall,
                color = barColor
            )
        }

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress          = { animatedProgress },
            modifier          = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 1.dp),
            color             = barColor,
            trackColor        = barColor.copy(alpha = 0.15f)
        )
    }
}

/** Format float: show 1 decimal only when needed */
private fun Float.formatted(): String =
    if (this == kotlin.math.floor(this.toDouble()).toFloat()) toInt().toString()
    else "%.1f".format(this)
