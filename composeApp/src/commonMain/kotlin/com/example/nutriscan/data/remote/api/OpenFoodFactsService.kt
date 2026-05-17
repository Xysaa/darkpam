package com.example.nutriscan.data.remote.api

import com.example.nutriscan.data.remote.dto.OpenFoodFactsResponse
import com.example.nutriscan.data.remote.dto.toDomain
import com.example.nutriscan.domain.model.Product
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

/**
 * Fetches product data from the OpenFoodFacts v2 API.
 *
 * Endpoint : GET https://world.openfoodfacts.org/api/v2/product/{barcode}.json
 * Fields   : hanya field yang kita pakai (product_name*, brands, image_front_small_url,
 *             serving_size, dan nutriments yang relevan).
 *
 * Filtering via ?fields= mengurangi payload dari ~200KB → ~2KB per produk.
 */
class OpenFoodFactsService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/product"

        // Hanya minta field yang kita butuhkan
        private const val FIELDS =
            "code,product_name,product_name_id,product_name_en,brands," +
            "image_front_small_url,image_url,serving_size," +
            "nutriments.energy-kcal_100g,nutriments.energy-kcal," +
            "nutriments.fat_100g,nutriments.saturated-fat_100g," +
            "nutriments.carbohydrates_100g,nutriments.sugars_100g," +
            "nutriments.sodium_100g,nutriments.salt_100g,nutriments.proteins_100g"
    }

    /**
     * @return Result.success(Product) jika barcode ditemukan dan data valid,
     *         Result.failure jika 404 / status != 1 / network error.
     */
    suspend fun fetchProduct(barcode: String): Result<Product> = runCatching {
        val response: OpenFoodFactsResponse = client.get("$BASE_URL/$barcode.json") {
            header("User-Agent", "NutriScan-KMP/1.0 (contact@example.com)")
            url { parameters.append("fields", FIELDS) }
        }.body()

        if (response.status != 1 || response.product == null) {
            throw Exception("Produk dengan barcode $barcode tidak ditemukan di database")
        }

        response.product.toDomain(barcode)
    }
}
