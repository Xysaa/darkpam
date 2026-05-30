package com.example.nutriscan.domain.model

/**
 * Ways the user can express how much of a product they consumed. Each unit maps
 * to an approximate weight in grams. [PORSI] / [SETENGAH_PORSI] are resolved at
 * runtime against the product's own serving size, so their [grams] here is a
 * neutral fallback used only when a serving size is unknown.
 */
enum class ServingUnit(
    val displayName: String,
    val defaultGrams: Float,
    /** When true the gram weight is derived from the product serving size. */
    val relativeToServing: Boolean = false,
    val servingFactor: Float = 1f
) {
    GRAM("Gram (g)", 1f),
    SENDOK_TEH("Sendok teh", 5f),
    SENDOK_MAKAN("Sendok makan", 15f),
    GELAS("Gelas", 240f),
    KEMASAN("Kemasan/Porsi", 100f, relativeToServing = true, servingFactor = 1f),
    SETENGAH_PORSI("½ Porsi", 50f, relativeToServing = true, servingFactor = 0.5f);

    /** Grams represented by one unit, given the product [servingSize] in grams. */
    fun gramsPerUnit(servingSize: Float): Float =
        if (relativeToServing) servingSize * servingFactor else defaultGrams
}

/**
 * A single logged consumption. Nutriment fields are already scaled to the
 * consumed [grams] (not per-100g), so daily totals are a plain sum.
 */
data class ConsumptionEntry(
    val id: Long = 0,
    val barcode: String = "",
    val productName: String,
    val amountLabel: String = "",
    val grams: Float = 0f,
    val calories: Float = 0f,
    val fat: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val consumedAt: Long = 0L
)
