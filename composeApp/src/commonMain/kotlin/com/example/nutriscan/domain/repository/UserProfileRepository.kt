package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    /** Emits the stored profile, or null if onboarding has not been completed. */
    fun getProfile(): Flow<UserProfile?>

    /** Insert a brand-new profile (called once during onboarding). */
    suspend fun saveProfile(profile: UserProfile)

    /** Overwrite all mutable fields of the existing profile. */
    suspend fun updateProfile(profile: UserProfile)

    /** Remove the profile (e.g. for testing / account reset). */
    suspend fun deleteProfile()

    /** True when at least one profile row exists. */
    suspend fun hasProfile(): Boolean
}
