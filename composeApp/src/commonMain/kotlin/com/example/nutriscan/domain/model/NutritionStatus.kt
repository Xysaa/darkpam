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
    /** Only the nutrients that triggered CAUTION/AVOID (used for warnings). */
    val warnings: List<NutrientWarning> = emptyList(),
    /** Every analysed nutrient (SAFE included) for the full detail breakdown. */
    val allNutrients: List<NutrientWarning> = emptyList(),
    val aiSuggestion: String? = null       // populated by Gemini (Sprint 3+)
) {
    val warningMessages: List<String>
        get() = warnings
            .filter { it.status != NutritionStatus.SAFE }
            .map { w ->
                "${w.nutrientName}: ${format1(w.valuePerServing)}${w.unit}/sajian " +
                "(${w.percentDailyValue.toInt()}% AKG) — ${w.status.displayName}"
            }
}

private fun format1(value: Float): String {
    val rounded = kotlin.math.round(value * 10f) / 10f
    if (rounded % 1f == 0f) return rounded.toInt().toString()
    val tenths = kotlin.math.round(kotlin.math.abs(rounded) * 10f).toInt()
    val sign = if (rounded < 0f) "-" else ""
    return "$sign${tenths / 10}.${tenths % 10}"
}
