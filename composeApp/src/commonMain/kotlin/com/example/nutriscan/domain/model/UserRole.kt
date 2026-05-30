package com.example.nutriscan.domain.model

/**
 * The two (dummy) account types in NutriScan. Selected at login.
 *
 * - [USER]         : a regular person scanning products & consulting nutritionists.
 * - [NUTRITIONIST] : an "ahli gizi" who replies to user consultations.
 */
enum class UserRole(val displayName: String) {
    USER("Pengguna"),
    NUTRITIONIST("Ahli Gizi");

    companion object {
        fun fromString(value: String?): UserRole =
            entries.find { it.name == value } ?: USER
    }
}
