package com.example.nutriscan.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Root response ─────────────────────────────────────────────────────────────

@Serializable
data class OpenFoodFactsResponse(
    val status: Int = 0,                          // 1 = found, 0 = not found
    val product: OFFProduct? = null
)

// ── Product ───────────────────────────────────────────────────────────────────

@Serializable
data class OFFProduct(
    val code: String? = null,

    // Names — try multiple fields; Indonesian products often only have generic name
    @SerialName("product_name")
    val productName: String? = null,

    @SerialName("product_name_id")
    val productNameId: String? = null,          // Indonesian locale

    @SerialName("product_name_en")
    val productNameEn: String? = null,

    val brands: String? = null,

    // Image — front thumbnail preferred to keep payload small
    @SerialName("image_front_small_url")
    val imageFrontSmallUrl: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    // Serving
    @SerialName("serving_size")
    val servingSize: String? = null,            // e.g. "30 g"

    // Nutriments — only the fields we actually use
    val nutriments: OFFNutriments? = null
)

// ── Nutriments ────────────────────────────────────────────────────────────────
// OpenFoodFacts stores values per-100g AND per-serving.
// We prefer _100g keys and scale ourselves (consistent with our domain model).

@Serializable
data class OFFNutriments(
    // Calories
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Double? = null,

    @SerialName("energy-kcal")
    val energyKcal: Double? = null,             // fallback

    // Fat
    @SerialName("fat_100g")
    val fat100g: Double? = null,

    // Saturated fat
    @SerialName("saturated-fat_100g")
    val saturatedFat100g: Double? = null,

    // Carbohydrates
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Double? = null,

    // Sugar
    @SerialName("sugars_100g")
    val sugars100g: Double? = null,

    // Sodium  (OFF stores mg/100g)
    @SerialName("sodium_100g")
    val sodium100g: Double? = null,

    // Salt → convert to sodium if sodium missing (sodium = salt / 2.5)
    @SerialName("salt_100g")
    val salt100g: Double? = null,

    // Protein
    @SerialName("proteins_100g")
    val proteins100g: Double? = null
)

// ── Mapper: OFFProduct → domain Product ──────────────────────────────────────

fun OFFProduct.toDomain(barcode: String): com.example.nutriscan.domain.model.Product {
    val n = nutriments

    // Best available name
    val name = listOfNotNull(productNameId, productName, productNameEn)
        .firstOrNull { it.isNotBlank() } ?: ""

    // Best available image
    val image = listOfNotNull(imageFrontSmallUrl, imageUrl)
        .firstOrNull { it.isNotBlank() } ?: ""

    // Parse serving size string → Float grams (e.g. "30 g" → 30f)
    val serving = servingSize
        ?.replace(Regex("[^0-9.]"), "")
        ?.toFloatOrNull()
        ?.takeIf { it > 0f } ?: 100f

    // Sodium: prefer sodium_100g, fallback to salt/2.5
    val sodiumMg = when {
        n?.sodium100g != null && n.sodium100g > 0.0 -> (n.sodium100g * 1000).toFloat()   // g→mg
        n?.salt100g   != null && n.salt100g   > 0.0 -> (n.salt100g * 1000 / 2.5f).toFloat()
        else -> 0f
    }

    return com.example.nutriscan.domain.model.Product(
        barcode     = barcode,
        name        = name,
        brand       = brands?.split(",")?.firstOrNull()?.trim() ?: "",
        imageUrl    = image,
        servingSize = serving,
        nutriments  = com.example.nutriscan.domain.model.Nutriments(
            calories     = (n?.energyKcal100g ?: n?.energyKcal ?: 0.0).toFloat(),
            fat          = (n?.fat100g ?: 0.0).toFloat(),
            saturatedFat = (n?.saturatedFat100g ?: 0.0).toFloat(),
            sugar        = (n?.sugars100g ?: 0.0).toFloat(),
            sodium       = sodiumMg,
            protein      = (n?.proteins100g ?: 0.0).toFloat(),
            carbs        = (n?.carbohydrates100g ?: 0.0).toFloat()
        )
    )
}
