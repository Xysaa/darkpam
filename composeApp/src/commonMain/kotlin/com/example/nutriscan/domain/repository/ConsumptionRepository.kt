package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.ConsumptionEntry
import kotlinx.coroutines.flow.Flow

/**
 * Stores what the user actually consumed (after choosing a portion on the scan
 * result screen). Daily/weekly nutrition on Beranda is derived from this log.
 */
interface ConsumptionRepository {
    /** Observe all consumption entries, newest first. */
    fun observeAll(): Flow<List<ConsumptionEntry>>

    /** Log a new consumption entry. */
    suspend fun add(entry: ConsumptionEntry)

    /** Remove a single entry. */
    suspend fun delete(id: Long)

    /** Clear the whole log. */
    suspend fun clear()
}
