package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.Product

interface ProductRepository {
    /** Fetch product details for [barcode] from the remote food database. */
    suspend fun getProduct(barcode: String): Result<Product>
}
