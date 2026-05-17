package com.example.nutriscan.data.repository

import com.example.nutriscan.data.local.NutriScanDatabase
import com.example.nutriscan.data.local.entity.ScanHistoryMapper.toDomain
import com.example.nutriscan.data.remote.api.OpenFoodFactsService
import com.example.nutriscan.domain.model.Nutriments
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val openFoodFactsService: OpenFoodFactsService,
    private val database: NutriScanDatabase
) : ProductRepository {

    override suspend fun getProductByBarcode(barcode: String): Result<Product> {
        // 1. Cek cache lokal dulu (ScanHistory)
        val cached = withContext(Dispatchers.Default) {
            database.scanHistoryQueries
                .getScanByBarcode(barcode)
                .executeAsOneOrNull()
                ?.toDomain()
                ?.product
        }
        if (cached != null) return Result.success(cached)

        // 2. Fetch dari OpenFoodFacts
        val apiResult = openFoodFactsService.fetchProduct(barcode)
        if (apiResult.isSuccess) return apiResult

        // 3. Fallback: kembalikan produk dummy agar ResultScreen tetap bisa
        //    menampilkan analisis dengan data kosong (tidak crash)
        return Result.success(dummyProduct(barcode))
    }

    private fun dummyProduct(barcode: String) = Product(
        barcode     = barcode,
        name        = "Produk Tidak Dikenal",
        brand       = "",
        imageUrl    = "",
        servingSize = 100f,
        nutriments  = Nutriments()
    )
}
