package com.example.nutriscan.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A soft, slightly tinted drop shadow used throughout the app to give cards a
 * lifted, modern "floating" feel. The tint colour bleeds into the ambient/spot
 * shadow on supported API levels (28+) for a premium look.
 */
fun Modifier.softShadow(
    elevation: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = Color(0xFF0E5C45),
    alpha: Float = 0.18f
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = color.copy(alpha = alpha),
    spotColor = color.copy(alpha = alpha)
)
