package com.example.lab9

import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeCart(): Flow<List<LineaPedido>>

    suspend fun setQuantity(
        productId: String,
        quantity: Int
    ): Result<Unit>

    suspend fun removeProduct(
        productId: String
    ): Result<Unit>

    suspend fun clearCart(): Result<Unit>
}