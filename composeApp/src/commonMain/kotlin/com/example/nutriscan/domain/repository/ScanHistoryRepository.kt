package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    fun getAllHistory(): Flow<List<ScanResult>>
    fun getRecentHistory(limit: Long = 10): Flow<List<ScanResult>>
    suspend fun getScanById(id: Long): ScanResult?
    suspend fun saveScan(scanResult: ScanResult)
    suspend fun deleteScan(id: Long)
    suspend fun clearHistory()
}
