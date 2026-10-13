package com.example.lab9

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: String,
    val name: String,
    val unitPrice: Double,
    val quantity: Int
)

@Serializable
data class CreateOrderDto(
    val lines: List<OrderLineDto>,
    val total: Double,
    val createdAt: String
)

@Serializable
data class OrderDto(
    val id: String,
    val lines: List<OrderLineDto>,
    val total: Double,
    val createdAt: String
)