package com.example.nutriscan.data.local.entity

import com.example.nutriscan.data.local.UserProfileEntity
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile

object UserProfileMapper {
    fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
        id               = id,
        name             = name,
        age              = age.toInt(),
        weight           = weight.toFloat(),
        height           = height.toFloat(),
        healthConditions = Disease.fromCsv(conditions),
        createdAt        = created_at,
        updatedAt        = updated_at
    )
}
