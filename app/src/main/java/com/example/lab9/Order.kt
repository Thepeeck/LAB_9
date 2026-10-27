package com.example.lab9

data class OrderLine(
    val productId: String,
    val name: String,
    val unitPrice: Double,
    val quantity: Int
)

data class OrderDraft(
    val lines: List<OrderLine>,
    val total: Double,
    val createdAt: String
)

data class Order(
    val id: String,
    val lines: List<OrderLine>,
    val total: Double,
    val createdAt: String
)

enum class OrderFailureReason {
    CONNECTION,
    NOT_FOUND,
    SERVER,
    INVALID_RESPONSE
}

class OrderRepositoryException(
    val reason: OrderFailureReason
) : Exception(reason.name)