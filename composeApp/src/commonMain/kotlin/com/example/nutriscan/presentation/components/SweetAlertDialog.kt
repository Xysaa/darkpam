package com.example.nutriscan.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.StatusAvoid
import com.example.nutriscan.presentation.theme.StatusCaution
import com.example.nutriscan.presentation.theme.StatusSafe

/** Visual flavour of a [SweetAlertDialog], à la SweetAlert2 on the web. */
enum class AlertType { SUCCESS, ERROR, WARNING, INFO }

private data class AlertVisuals(val icon: ImageVector, val color: Color, val brush: Brush)

@Composable
private fun AlertType.visuals(): AlertVisuals = when (this) {
    AlertType.SUCCESS -> AlertVisuals(Icons.Filled.CheckCircle, StatusSafe, AppGradients.safe)
    AlertType.ERROR   -> AlertVisuals(Icons.Filled.Close, StatusAvoid, AppGradients.avoid)
    AlertType.WARNING -> AlertVisuals(Icons.Filled.WarningAmber, StatusCaution, AppGradients.caution)
    AlertType.INFO    -> AlertVisuals(Icons.Filled.Info, MaterialTheme.colorScheme.primary, AppGradients.brand)
}

/**
 * A centered, animated alert dialog with a coloured icon badge, title, message
 * and up to two actions. Mirrors the look-and-feel of web SweetAlert popups.
 *
 * @param type          drives the icon + colour
 * @param title         bold heading
 * @param message       supporting text
 * @param confirmText   primary button label
 * @param onConfirm     primary button action (also used as default dismiss)
 * @param dismissText   optional secondary button label
 * @param onDismiss     called on secondary button / back / outside tap
 */
@Composable
fun SweetAlertDialog(
    type: AlertType,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    dismissOnClickOutside: Boolean = true
) {
    val visuals = type.visuals()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "alertScale"
    )

    Dialog(
        onDismissRequest = { (onDismiss ?: onConfirm)() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(visuals.brush),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            if (dismissText != null && onDismiss != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dismissText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    GradientButton(
                        text = confirmText,
                        onClick = onConfirm,
                        brush = visuals.brush,
                        height = 48.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                GradientButton(
                    text = confirmText,
                    onClick = onConfirm,
                    brush = visuals.brush,
                    height = 48.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
