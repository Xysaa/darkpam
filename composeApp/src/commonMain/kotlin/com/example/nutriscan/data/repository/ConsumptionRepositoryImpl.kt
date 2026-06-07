package com.example.nutriscan.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.nutriscan.data.local.NutriScanDatabase
import com.example.nutriscan.data.local.entity.ConsumptionMapper.toDomain
import com.example.nutriscan.domain.model.ConsumptionEntry
import com.example.nutriscan.domain.repository.ConsumptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ConsumptionRepositoryImpl(
    private val database: NutriScanDatabase
) : ConsumptionRepository {

    private val queries = database.consumptionQueries

    override fun observeAll(): Flow<List<ConsumptionEntry>> =
        queries.getAllConsumption()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun add(entry: ConsumptionEntry) = withContext(Dispatchers.Default) {
        val ts = entry.consumedAt.takeIf { it != 0L } ?: Clock.System.now().toEpochMilliseconds()
        queries.insertConsumption(
            barcode = entry.barcode,
            product_name = entry.productName,
            amount_label = entry.amountLabel,
            grams = entry.grams.toDouble(),
            calories = entry.calories.toDouble(),
            fat = entry.fat.toDouble(),
            sugar = entry.sugar.toDouble(),
            sodium = entry.sodium.toDouble(),
            protein = entry.protein.toDouble(),
            carbs = entry.carbs.toDouble(),
            consumed_at = ts
        )
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteConsumptionById(id)
    }

    override suspend fun clear() = withContext(Dispatchers.Default) {
        queries.clearConsumption()
    }
}
