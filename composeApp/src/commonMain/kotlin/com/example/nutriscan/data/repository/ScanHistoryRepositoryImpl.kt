package com.example.nutriscan.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.nutriscan.data.local.NutriScanDatabase
import com.example.nutriscan.data.local.entity.toDomain
import com.example.nutriscan.domain.model.ScanResult
import com.example.nutriscan.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ScanHistoryRepositoryImpl(
    private val database: NutriScanDatabase
) : ScanHistoryRepository {

    private val queries = database.scanHistoryEntityQueries

    override fun getAllHistory(): Flow<List<ScanResult>> =
        queries.getAllHistory()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getRecentHistory(limit: Long): Flow<List<ScanResult>> =
        queries.getRecentHistory(limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getScanById(id: Long): ScanResult? =
        withContext(Dispatchers.IO) {
            queries.getScanById(id).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun saveScan(scanResult: ScanResult) =
        withContext(Dispatchers.IO) {
            val p = scanResult.product
            val n = p.nutriments
            val a = scanResult.analysis
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insertScan(
                barcode       = p.barcode,
                product_name  = p.name,
                brand         = p.brand,
                image_url     = p.imageUrl,
                calories      = n.calories.toDouble(),
                fat           = n.fat.toDouble(),
                saturated_fat = n.saturatedFat.toDouble(),
                sugar         = n.sugar.toDouble(),
                sodium        = n.sodium.toDouble(),
                protein       = n.protein.toDouble(),
                carbs         = n.carbs.toDouble(),
                serving_size  = p.servingSize.toDouble(),
                status        = a.overallStatus.name,
                warnings      = a.warningMessages.joinToString(","),
                scanned_at    = now
            )
        }

    override suspend fun deleteScan(id: Long) =
        withContext(Dispatchers.IO) { queries.deleteScanById(id) }

    override suspend fun clearHistory() =
        withContext(Dispatchers.IO) { queries.clearHistory() }
}
