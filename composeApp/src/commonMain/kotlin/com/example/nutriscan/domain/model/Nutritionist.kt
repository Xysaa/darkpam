package com.example.nutriscan.domain.model

/**
 * A consultable nutritionist ("ahli gizi"). For this local/dummy build the
 * catalog is static (see ConsultationRepository). Each consultation costs
 * [pricePerChat] coins (15–35 range).
 */
data class Nutritionist(
    val id: String,
    val name: String,
    val specialty: String,
    val bio: String,
    val experienceYears: Int,
    val rating: Double,
    val reviewCount: Int,
    val pricePerChat: Int,        // cost in coins to open a consultation
    val isOnline: Boolean = true
) {
    /** Two-letter initials for avatar fallback, e.g. "Dr. Sinta Wijaya" -> "SW". */
    val initials: String
        get() {
            val cleaned = name.removePrefix("Dr. ").removePrefix("dr. ").trim()
            val parts = cleaned.split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else            -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }
}
