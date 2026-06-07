package com.example.nutriscan.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Centralised gradient brushes for the NutriScan brand.
 *
 * These are plain (non-composable) values so they can be reused freely inside
 * `Modifier.background(...)`, buttons, headers, and decorative shapes.
 */
object AppGradients {

    /** Primary brand gradient — emerald → teal. Used for headers, hero cards, primary buttons. */
    val brand: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF00C389), Color(0xFF009C9C))
    )

    /** Softer brand gradient for large surfaces / hero backgrounds. */
    val brandSoft: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF12B886), Color(0xFF0CA6A6), Color(0xFF1E9E84))
    )

    /** Warm gradient for "energy / calories" accents. */
    val energy: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
    )

    /** Gold gradient for coin / reward visuals. */
    val coin: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD86E), Color(0xFFF9A825))
    )

    /** Safe (green) status gradient. */
    val safe: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF2BD27E), Color(0xFF12A45F))
    )

    /** Caution (amber) status gradient. */
    val caution: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFC861), Color(0xFFF5972A))
    )

    /** Avoid (red) status gradient. */
    val avoid: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6A6A), Color(0xFFE5484D))
    )

    /** Subtle vertical fade used behind scrollable content (light theme). */
    val pageLight: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF1FBF6), Color(0xFFFFFFFF))
    )

    /** Subtle vertical fade behind scrollable content (dark theme). */
    val pageDark: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF111A16), Color(0xFF0D1411))
    )

    /** Generic helper: vertical translucent overlay (for image scrims / camera). */
    fun scrim(): Brush = Brush.verticalGradient(
        colors = listOf(Color(0x00000000), Color(0x99000000))
    )
}
