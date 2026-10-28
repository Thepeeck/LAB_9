
package com.example.lab9

import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>

    suspend fun reserveUnit(
        productId: String
    ): Result<Unit>

    suspend fun releaseUnits(
        productId: String,
        quantity: Int
    ): Result<Unit>
}
