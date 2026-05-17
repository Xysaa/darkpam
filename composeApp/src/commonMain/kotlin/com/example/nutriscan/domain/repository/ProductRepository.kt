package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.Product

interface ProductRepository {
    /**
     * Fetch product by barcode.
     * Strategy: local cache (ScanHistory) → OpenFoodFacts API → dummy fallback.
     */
    suspend fun getProductByBarcode(barcode: String): Result<Product>
}
