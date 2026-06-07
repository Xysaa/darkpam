package com.example.nutriscan.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nutriscan.presentation.theme.AppGradients

// ============================================================================
//  Lightweight custom charts (no external dependency) used on the Home screen.
// ============================================================================

/**
 * A circular progress ring (donut) with a gradient sweep and centred content.
 *
 * @param progress 0f..1f (values above 1 are clamped).
 */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 150.dp,
    strokeWidth: Dp = 16.dp,
    brush: Brush = AppGradients.brand,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    centerContent: @Composable () -> Unit = {}
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "ringProgress"
    )
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (animated > 0f) {
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

data class BarEntry(val label: String, val value: Float, val highlighted: Boolean = false)

/**
 * A simple animated bar chart (e.g. scans per day of the week).
 */
@Composable
fun WeeklyBarChart(
    data: List<BarEntry>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    barBrush: Brush = AppGradients.brand
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { entry ->
            val fraction = (entry.value / maxValue).coerceIn(0f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(700),
                label = "bar_${entry.label}"
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (entry.value > 0f) {
                    Text(
                        text = entry.value.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Box(
                    modifier = Modifier
                        .height(chartHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedFraction.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (entry.value > 0f) barBrush
                                else Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (entry.highlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.highlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class StatusSlice(val label: String, val value: Int, val color: Color)

/**
 * A horizontal stacked proportion bar with a legend below (status distribution).
 */
@Composable
fun StatusBreakdownBar(
    slices: List<StatusSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            slices.forEach { slice ->
                if (slice.value > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(slice.value.toFloat() / total.toFloat())
                            .background(slice.color)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            slices.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(slice.color)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${slice.label} ${slice.value}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
