package com.example.nutriscan.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class Result(val barcode: String) : Route
}
