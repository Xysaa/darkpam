package com.example.nutriscan.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Response wrapper for OpenFoodFacts API v2:
 * GET https://world.openfoodfacts.org/api/v2/product/{barcode}.json
 *
 * `status` == 1 means the product was found.
 */
@Serializable
data class OffResponse(
    val status: Int = 0,
    @SerialName("status_verbose") val statusVerbose: String? = null,
    val product: OffProduct? = null
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_id") val productNameId: String? = null,
    val brands: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_front_url") val imageFrontUrl: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    // The nutriments object has dozens of dynamically-named keys (e.g.
    // "energy-kcal_100g"); we read them defensively as a raw JSON object.
    val nutriments: JsonObject? = null
)
