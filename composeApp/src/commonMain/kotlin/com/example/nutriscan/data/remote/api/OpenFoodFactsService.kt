package com.example.nutriscan.data.remote.api

import com.example.nutriscan.data.remote.dto.OffResponse
import com.example.nutriscan.domain.model.Nutriments
import com.example.nutriscan.domain.model.Product
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * Fetches product data from the free OpenFoodFacts database.
 * No API key is required for reasonable usage.
 */
class OpenFoodFactsService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v2"
        private const val FIELDS = "product_name,brands,image_url,image_front_url,serving_size,nutriments"
    }

    /** Returns the mapped [Product], or null if the barcode is unknown. */
    suspend fun getProduct(barcode: String): Product? {
        val response: OffResponse = client.get("$BASE_URL/product/$barcode.json") {
            parameter("fields", FIELDS)
            // OFF recommends identifying the app via User-Agent.
            header("User-Agent", "NutriScan/1.0 (Android; KMP demo)")
        }.body()

        val product = response.product ?: return null
        if (response.status != 1) return null

        val n = product.nutriments

        // OFF stores sodium in grams/100g; our model uses mg. Fall back to salt.
        val sodiumMg: Float = run {
            val sodium = n.readDouble("sodium_100g")
            when {
                sodium != null && sodium > 0.0 -> (sodium * 1000.0).toFloat()
                else -> {
                    val salt = n.readDouble("salt_100g")
                    if (salt != null && salt > 0.0) ((salt / 2.5) * 1000.0).toFloat() else 0f
                }
            }
        }

        return Product(
            barcode = barcode,
            name = product.productName?.takeIf { it.isNotBlank() } ?: "Produk Tidak Dikenal",
            brand = product.brands?.takeIf { it.isNotBlank() }?.substringBefore(",")?.trim() ?: "",
            imageUrl = (product.imageUrl ?: product.imageFrontUrl).orEmpty(),
            servingSize = parseServingSize(product.servingSize),
            nutriments = Nutriments(
                calories = n.readFloat("energy-kcal_100g"),
                fat = n.readFloat("fat_100g"),
                saturatedFat = n.readFloat("saturated-fat_100g"),
                sugar = n.readFloat("sugars_100g"),
                sodium = sodiumMg,
                protein = n.readFloat("proteins_100g"),
                carbs = n.readFloat("carbohydrates_100g")
            )
        )
    }

    private fun JsonObject?.readDouble(key: String): Double? =
        (this?.get(key) as? JsonPrimitive)?.doubleOrNull

    private fun JsonObject?.readFloat(key: String): Float =
        readDouble(key)?.toFloat() ?: 0f

    /** Extract the first numeric value from a serving string like "30 g". */
    private fun parseServingSize(raw: String?): Float {
        if (raw.isNullOrBlank()) return 100f
        val match = Regex("""\d+([.,]\d+)?""").find(raw) ?: return 100f
        return match.value.replace(',', '.').toFloatOrNull()?.takeIf { it > 0f } ?: 100f
    }
}
