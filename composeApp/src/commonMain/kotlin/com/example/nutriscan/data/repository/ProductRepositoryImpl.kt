package com.example.nutriscan.data.repository

import com.example.nutriscan.data.remote.api.OpenFoodFactsService
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val service: OpenFoodFactsService
) : ProductRepository {

    override suspend fun getProduct(barcode: String): Result<Product> = runCatching {
        service.getProduct(barcode)
            ?: throw NoSuchElementException("Produk dengan barcode $barcode tidak ditemukan di OpenFoodFacts.")
    }
}
