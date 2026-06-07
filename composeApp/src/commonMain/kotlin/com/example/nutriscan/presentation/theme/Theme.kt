package com.example.nutriscan.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==================== COLOR SCHEMES ====================

private val LightColors = lightColorScheme(
    primary              = Emerald,
    onPrimary            = Color.White,
    primaryContainer     = MintContainer,
    onPrimaryContainer   = EmeraldDark,
    secondary            = Teal,
    onSecondary          = Color.White,
    secondaryContainer   = TealContainer,
    onSecondaryContainer = Color(0xFF003B3B),
    tertiary             = Coral,
    onTertiary           = Color.White,
    tertiaryContainer    = CoralLight,
    onTertiaryContainer  = Color(0xFF5C1B0B),
    error                = StatusAvoid,
    onError              = androidx.compose.ui.graphics.Color.White,
    errorContainer       = StatusAvoidBg,
    onErrorContainer     = Color(0xFF6E1414),
    background           = BackgroundLight,
    onBackground         = OnBackgroundLight,
    surface              = SurfaceLight,
    onSurface            = OnSurfaceLight,
    surfaceVariant       = SurfaceVariantLight,
    onSurfaceVariant     = OnSurfaceVariantLight,
    outline              = OutlineLight,
    outlineVariant       = Color(0xFFD3E5DC),
)

private val DarkColors = darkColorScheme(
    primary              = EmeraldLight,
    onPrimary            = Color(0xFF003828),
    primaryContainer     = EmeraldDark,
    onPrimaryContainer   = MintContainer,
    secondary            = TealLight,
    onSecondary          = Color(0xFF003434),
    secondaryContainer   = Color(0xFF00504F),
    onSecondaryContainer = TealContainer,
    tertiary             = CoralLight,
    onTertiary           = Color(0xFF591B0A),
    tertiaryContainer    = Color(0xFF7A2D16),
    onTertiaryContainer  = CoralLight,
    error                = Color(0xFFFF8A8A),
    onError              = Color(0xFF5C0E10),
    errorContainer       = Color(0xFF8C2326),
    onErrorContainer     = Color(0xFFFFDAD9),
    background           = BackgroundDark,
    onBackground         = OnBackgroundDark,
    surface              = SurfaceDark,
    onSurface            = OnSurfaceDark,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = OnSurfaceVariantDark,
    outline              = OutlineDark,
    outlineVariant       = Color(0xFF334039),
)

// ==================== SHAPES ====================

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// ==================== THEME ====================

@Composable
fun NutriScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes      = AppShapes,
        typography  = AppTypography,
        content     = content
    )
}
