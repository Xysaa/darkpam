package com.example.nutriscan.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destinations. Login & Onboarding are handled at the App root
 * (driven by session state), so they are not part of the in-app NavHosts.
 */
sealed interface Route {

    // ── User app tabs ──
    @Serializable
    data object Home : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Consultation : Route

    @Serializable
    data object Profile : Route

    // ── User app stack destinations ──
    @Serializable
    data object Scan : Route

    @Serializable
    data class Result(val barcode: String) : Route

    // ── Shared / consultation ──
    @Serializable
    data class Chat(val conversationId: Long) : Route

    // ── Nutritionist app ──
    @Serializable
    data object NutritionistHome : Route
}

/** Extract the simple destination name ("Home", "Result", "Chat", ...) from a
 *  navigation-compose route string, robust across library versions. */
fun routeNameOf(route: String?): String? =
    route?.substringBefore("/")?.substringBefore("?")?.substringAfterLast(".")
