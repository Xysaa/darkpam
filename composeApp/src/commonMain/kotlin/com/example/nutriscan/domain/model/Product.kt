package com.example.nutriscan.domain.model

data class Nutriments(
    val calories: Float    = 0f,   // kcal per 100g
    val fat: Float         = 0f,   // g per 100g
    val saturatedFat: Float = 0f,  // g per 100g
    val sugar: Float       = 0f,   // g per 100g
    val sodium: Float      = 0f,   // mg per 100g
    val protein: Float     = 0f,   // g per 100g
    val carbs: Float       = 0f    // g per 100g
)

data class Product(
    val barcode: String,
    val name: String,
    val brand: String      = "",
    val imageUrl: String   = "",
    val nutriments: Nutriments = Nutriments(),
    val servingSize: Float = 100f  // g; default 100g if not specified
) {
    val displayName: String
        get() = name.ifBlank { "Produk Tidak Dikenal" }

    /** Scale nutriments to actual serving size */
    val nutrimentsPerServing: Nutriments
        get() {
            val factor = servingSize / 100f
            return Nutriments(
                calories    = nutriments.calories    * factor,
                fat         = nutriments.fat         * factor,
                saturatedFat = nutriments.saturatedFat * factor,
                sugar       = nutriments.sugar       * factor,
                sodium      = nutriments.sodium      * factor,
                protein     = nutriments.protein     * factor,
                carbs       = nutriments.carbs       * factor
            )
        }
}

data class ScanResult(
    val id: Long           = 0,
    val product: Product,
    val analysis: NutritionAnalysis,
    val scannedAt: Long    = 0L
)
