package com.example.nutriscan.domain.usecase

import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

// ==================== GET PROFILE ====================

class GetUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile?> = repository.getProfile()
}

// ==================== SAVE PROFILE (onboarding) ====================

class SaveUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> = runCatching {
        validate(profile)
        repository.saveProfile(profile)
    }

    private fun validate(profile: UserProfile) {
        require(profile.name.isNotBlank())         { "Nama tidak boleh kosong" }
        require(profile.age in 1..120)             { "Usia harus antara 1–120 tahun" }
        require(profile.weight in 1f..500f)        { "Berat badan tidak valid" }
        require(profile.height in 50f..300f)       { "Tinggi badan tidak valid" }
    }
}

// ==================== UPDATE PROFILE (profile screen) ====================

class UpdateUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> = runCatching {
        validate(profile)
        repository.updateProfile(profile)
    }

    private fun validate(profile: UserProfile) {
        require(profile.name.isNotBlank())         { "Nama tidak boleh kosong" }
        require(profile.age in 1..120)             { "Usia harus antara 1–120 tahun" }
        require(profile.weight in 1f..500f)        { "Berat badan tidak valid" }
        require(profile.height in 50f..300f)       { "Tinggi badan tidak valid" }
    }
}

// ==================== DELETE PROFILE ====================

class DeleteUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        repository.deleteProfile()
    }
}

// ==================== HAS PROFILE ====================

class HasUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): Boolean = repository.hasProfile()
}
