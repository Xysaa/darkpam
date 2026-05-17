package com.example.nutriscan.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.nutriscan.data.local.NutriScanDatabase
import com.example.nutriscan.data.local.entity.toDomain
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class UserProfileRepositoryImpl(
    private val database: NutriScanDatabase
) : UserProfileRepository {

    private val queries = database.userProfileEntityQueries

    override fun getProfile(): Flow<UserProfile?> =
        queries.selectProfile()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insertProfile(
                name       = profile.name,
                age        = profile.age.toLong(),
                weight     = profile.weight.toDouble(),
                height     = profile.height.toDouble(),
                conditions = Disease.toCsv(profile.healthConditions),
                created_at = now,
                updated_at = now
            )
        }

    override suspend fun updateProfile(profile: UserProfile) =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateProfile(
                name       = profile.name,
                age        = profile.age.toLong(),
                weight     = profile.weight.toDouble(),
                height     = profile.height.toDouble(),
                conditions = Disease.toCsv(profile.healthConditions),
                updated_at = now,
                id         = profile.id
            )
        }

    override suspend fun deleteProfile() =
        withContext(Dispatchers.IO) { queries.deleteProfile() }

    override suspend fun hasProfile(): Boolean =
        withContext(Dispatchers.IO) {
            queries.countProfiles().executeAsOne() > 0
        }
}
