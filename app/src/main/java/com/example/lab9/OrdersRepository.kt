package com.example.lab9

import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun observeOrders(): Flow<List<Order>>

    fun observeOrder(
        orderId: String
    ): Flow<Order?>

    suspend fun refreshOrders(): Result<Unit>

    suspend fun refreshOrder(
        orderId: String
    ): Result<Unit>

    suspend fun submitOrder(
        draft: OrderDraft
    ): Result<Order>
}