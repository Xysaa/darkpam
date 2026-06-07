package com.example.nutriscan.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Slightly punchier typography than the Material default — bolder display /
 * headline weights and a touch more letter spacing for a polished, branded feel.
 * Uses the platform default font family (no custom font resources required).
 */
private val base = Typography()

val AppTypography = Typography(
    displaySmall   = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge  = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    headlineSmall  = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    titleLarge     = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium    = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall     = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge     = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    labelMedium    = base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge      = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
)
