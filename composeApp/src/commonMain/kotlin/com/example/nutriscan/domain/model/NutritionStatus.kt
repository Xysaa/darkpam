package com.example.nutriscan.domain.model

/** Overall safety status of a scanned product for the current user */
enum class NutritionStatus(val displayName: String, val emoji: String) {
    SAFE("Aman", "✅"),
    CAUTION("Perhatian", "⚠️"),
    AVOID("Hindari", "🚫");

    companion object {
        fun fromString(value: String): NutritionStatus =
            entries.find { it.name == value } ?: SAFE
    }
}

/** Per-nutrient analysis result */
data class NutrientWarning(
    val nutrientName: String,
    val valuePerServing: Float,   // g or mg
    val unit: String,             // "g" or "mg"
    val percentDailyValue: Float, // 0..100+
    val status: NutritionStatus
)

/** Full analysis result attached to a scan */
data class NutritionAnalysis(
    val overallStatus: NutritionStatus,
    val warnings: List<NutrientWarning> = emptyList(),
    val aiSuggestion: String? = null       // populated by Gemini (Sprint 3+)
) {
    val warningMessages: List<String>
        get() = warnings
            .filter { it.status != NutritionStatus.SAFE }
            .map { w ->
                "${w.nutrientName}: ${w.valuePerServing}${w.unit}/sajian " +
                "(${w.percentDailyValue.toInt()}% AKG) — ${w.status.displayName}"
            }
}
