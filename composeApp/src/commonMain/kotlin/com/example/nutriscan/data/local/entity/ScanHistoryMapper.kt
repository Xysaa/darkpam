package com.example.nutriscan.data.local.entity

import com.example.nutriscan.data.local.ScanHistoryEntity
import com.example.nutriscan.domain.model.Nutriments
import com.example.nutriscan.domain.model.NutritionAnalysis
import com.example.nutriscan.domain.model.NutritionStatus
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.ScanResult

object ScanHistoryMapper {
    fun ScanHistoryEntity.toDomain(): ScanResult {
        val product = Product(
            barcode      = barcode,
            name         = product_name,
            brand        = brand,
            imageUrl     = image_url,
            servingSize  = serving_size.toFloat(),
            nutriments   = Nutriments(
                calories     = calories.toFloat(),
                fat          = fat.toFloat(),
                saturatedFat = saturated_fat.toFloat(),
                sugar        = sugar.toFloat(),
                sodium       = sodium.toFloat(),
                protein      = protein.toFloat(),
                carbs        = carbs.toFloat()
            )
        )

        val analysis = NutritionAnalysis(
            overallStatus = NutritionStatus.fromString(status),
            warnings      = emptyList(),
            aiSuggestion  = null
        )

        return ScanResult(
            id        = id,
            product   = product,
            analysis  = analysis,
            scannedAt = scanned_at
        )
    }
}
