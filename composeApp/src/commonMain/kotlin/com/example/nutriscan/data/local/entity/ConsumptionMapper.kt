package com.example.nutriscan.data.local.entity

import com.example.nutriscan.data.local.ConsumptionEntity
import com.example.nutriscan.domain.model.ConsumptionEntry

object ConsumptionMapper {
    fun ConsumptionEntity.toDomain(): ConsumptionEntry = ConsumptionEntry(
        id = id,
        barcode = barcode,
        productName = product_name,
        amountLabel = amount_label,
        grams = grams.toFloat(),
        calories = calories.toFloat(),
        fat = fat.toFloat(),
        sugar = sugar.toFloat(),
        sodium = sodium.toFloat(),
        protein = protein.toFloat(),
        carbs = carbs.toFloat(),
        consumedAt = consumed_at
    )
}
