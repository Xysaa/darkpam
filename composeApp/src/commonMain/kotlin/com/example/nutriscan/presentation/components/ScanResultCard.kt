package com.example.nutriscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutriscan.domain.model.ScanResult
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** A reusable card representing a single scanned product. */
@Composable
fun ScanResultCard(
    scan: ScanResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTime: Boolean = false
) {
    SoftCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fastfood,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.product.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val secondary = when {
                    scan.product.brand.isNotBlank() && showTime ->
                        "${scan.product.brand} • ${formatTimestamp(scan.scannedAt)}"
                    scan.product.brand.isNotBlank() -> scan.product.brand
                    showTime -> formatTimestamp(scan.scannedAt)
                    else -> "Barcode ${scan.product.barcode}"
                }
                if (secondary.isNotBlank()) {
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.size(4.dp))
            StatusChip(status = scan.analysis.overallStatus)
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val local = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dd = local.dayOfMonth.toString().padStart(2, '0')
    val mm = local.monthNumber.toString().padStart(2, '0')
    val hh = local.hour.toString().padStart(2, '0')
    val min = local.minute.toString().padStart(2, '0')
    return "$dd/$mm ${hh}:${min}"
}
