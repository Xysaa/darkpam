package com.example.nutriscan.domain.usecase

import com.example.nutriscan.domain.model.Nutriments
import com.example.nutriscan.domain.model.NutrientWarning
import com.example.nutriscan.domain.model.NutritionAnalysis
import com.example.nutriscan.domain.model.NutritionStatus
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.UserProfile

/**
 * Core business logic: compare a product's nutritional values against
 * the user's personalised daily needs and return a NutritionAnalysis.
 *
 * Thresholds (per serving as % of daily AKG):
 *   CAUTION  > 20%
 *   AVOID    > 35%
 *
 * Disease adjustments (halve the threshold):
 *   Diabetes      → sugar thresholds ÷ 2
 *   Hypertension  → sodium thresholds ÷ 2
 *   Obesity/Heart → saturatedFat + calorie thresholds ÷ 2
 *   KidneyDisease → protein thresholds ÷ 2
 */
class AnalyzeNutritionUseCase {

    operator fun invoke(product: Product, user: UserProfile): NutritionAnalysis {
        val serving = product.nutrimentsPerServing

        val dailyValues = DailyValues(
            calories    = user.dailyCalorieNeed,
            fat         = user.dailyCalorieNeed * 0.30f / 9f,        // 30% from fat, 9 kcal/g
            saturatedFat = user.dailyCalorieNeed * 0.10f / 9f,       // 10% from sat fat
            sugar       = if (user.hasDiabetes) 12.5f else 25f,      // g/day; halved for diabetes
            sodium      = if (user.hasHypertension) 1000f else 2000f, // mg/day; halved for hypertension
            protein     = if (user.hasKidneyDisease) 0.4f * user.weight else 0.8f * user.weight,
            carbs       = user.dailyCalorieNeed * 0.55f / 4f          // 55% from carbs, 4 kcal/g
        )

        val saturatedFatDv = if (user.hasObesity || user.hasHeartDisease)
            dailyValues.saturatedFat / 2f else dailyValues.saturatedFat

        val warnings = mutableListOf<NutrientWarning>()

        warnings += nutrientWarning("Kalori",           serving.calories,    "kcal", dailyValues.calories,     user.hasObesity || user.hasHeartDisease)
        warnings += nutrientWarning("Lemak Total",      serving.fat,         "g",    dailyValues.fat,           user.hasObesity || user.hasHeartDisease)
        warnings += nutrientWarning("Lemak Jenuh",      serving.saturatedFat,"g",    saturatedFatDv,            false)
        warnings += nutrientWarning("Gula",             serving.sugar,       "g",    dailyValues.sugar,         false)
        warnings += nutrientWarning("Natrium",          serving.sodium,      "mg",   dailyValues.sodium,        false)
        warnings += nutrientWarning("Protein",          serving.protein,     "g",    dailyValues.protein,       false)

        val overallStatus = when {
            warnings.any { it.status == NutritionStatus.AVOID }   -> NutritionStatus.AVOID
            warnings.any { it.status == NutritionStatus.CAUTION } -> NutritionStatus.CAUTION
            else                                                   -> NutritionStatus.SAFE
        }

        return NutritionAnalysis(
            overallStatus = overallStatus,
            warnings      = warnings.filter { it.status != NutritionStatus.SAFE }
        )
    }

    private fun nutrientWarning(
        name: String,
        valuePerServing: Float,
        unit: String,
        dailyValue: Float,
        halveThreshold: Boolean
    ): NutrientWarning {
        val cautionThreshold = if (halveThreshold) 0.10f else 0.20f
        val avoidThreshold   = if (halveThreshold) 0.175f else 0.35f
        val pct = if (dailyValue > 0f) (valuePerServing / dailyValue) * 100f else 0f

        val status = when {
            pct > avoidThreshold * 100f   -> NutritionStatus.AVOID
            pct > cautionThreshold * 100f -> NutritionStatus.CAUTION
            else                          -> NutritionStatus.SAFE
        }

        return NutrientWarning(
            nutrientName      = name,
            valuePerServing   = valuePerServing,
            unit              = unit,
            percentDailyValue = pct,
            status            = status
        )
    }

    private data class DailyValues(
        val calories: Float,
        val fat: Float,
        val saturatedFat: Float,
        val sugar: Float,
        val sodium: Float,
        val protein: Float,
        val carbs: Float
    )
}
