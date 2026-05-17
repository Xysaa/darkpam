package com.example.nutriscan.domain.model

import kotlin.math.pow

data class UserProfile(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val weight: Float,   // kg
    val height: Float,   // cm
    val healthConditions: List<Disease> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    /** Body Mass Index */
    val bmi: Float
        get() {
            val heightM = height / 100f
            return if (heightM > 0f) weight / heightM.pow(2) else 0f
        }

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "Kurus"
            bmi < 25.0f -> "Normal"
            bmi < 30.0f -> "Kelebihan Berat Badan"
            else        -> "Obesitas"
        }

    val hasDiabetes: Boolean
        get() = Disease.DIABETES in healthConditions

    val hasHypertension: Boolean
        get() = Disease.HYPERTENSION in healthConditions

    val hasObesity: Boolean
        get() = Disease.OBESITY in healthConditions || bmi >= 30f

    val hasHeartDisease: Boolean
        get() = Disease.HEART_DISEASE in healthConditions

    val hasKidneyDisease: Boolean
        get() = Disease.KIDNEY_DISEASE in healthConditions

    /** Daily calorie need estimate (Mifflin-St Jeor, sedentary) */
    val dailyCalorieNeed: Float
        get() {
            // Using unisex average; can be improved if gender field is added later
            val bmr = (10f * weight) + (6.25f * height) - (5f * age) + 5f // male approximation
            return bmr * 1.2f  // sedentary activity factor
        }
}
