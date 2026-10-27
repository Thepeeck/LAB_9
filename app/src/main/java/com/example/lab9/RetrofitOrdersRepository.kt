package com.example.lab9

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class RetrofitOrdersRepository(
    private val api: OrderApi
) : OrdersRepository {

    private val orders = MutableStateFlow<List<Order>>(emptyList())

    private val orderDetails =
        MutableStateFlow<Map<String, Order>>(emptyMap())

    override fun observeOrders(): Flow<List<Order>> {
        return orders.asStateFlow()
    }

    override fun observeOrder(
        orderId: String
    ): Flow<Order?> {
        return orderDetails
            .map { details ->
                details[orderId]
            }
            .distinctUntilChanged()
    }

    override suspend fun refreshOrders(): Result<Unit> {
        return executeRequest {
            val response = api.getOrders(
                sortBy = "createdAt",
                order = "desc"
            )

            orders.value = response.map { dto ->
                dto.toDomain()
            }
        }
    }

    override suspend fun refreshOrder(
        orderId: String
    ): Result<Unit> {
        return executeRequest {
            try {
                val order = api.getOrder(orderId).toDomain()

                orderDetails.update { current ->
                    current + (orderId to order)
                }
            } catch (exception: HttpException) {
                if (exception.code() == 404) {
                    orderDetails.update { current ->
                        current - orderId
                    }
                }

                throw exception
            }
        }
    }

    override suspend fun submitOrder(
        draft: OrderDraft
    ): Result<Order> {
        return executeRequest {
            val request = CreateOrderDto(
                lines = draft.lines.map { line ->
                    OrderLineDto(
                        productId = line.productId,
                        name = line.name,
                        unitPrice = line.unitPrice,
                        quantity = line.quantity
                    )
                },
                total = draft.total,
                createdAt = draft.createdAt
            )

            val savedOrder = api.createOrder(request).toDomain()

            orderDetails.update { current ->
                current + (savedOrder.id to savedOrder)
            }

            savedOrder
        }
    }

    private fun OrderDto.toDomain(): Order {
        return Order(
            id = id,
            lines = lines.map { line ->
                OrderLine(
                    productId = line.productId,
                    name = line.name,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity
                )
            },
            total = total,
            createdAt = createdAt
        )
    }

    private suspend fun <T> executeRequest(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            failure(OrderFailureReason.CONNECTION)
        } catch (exception: HttpException) {
            val reason = if (exception.code() == 404) {
                OrderFailureReason.NOT_FOUND
            } else {
                OrderFailureReason.SERVER
            }

            failure(reason)
        } catch (exception: SerializationException) {
            failure(OrderFailureReason.INVALID_RESPONSE)
        }
    }

    private fun <T> failure(
        reason: OrderFailureReason
    ): Result<T> {
        return Result.failure(
            OrderRepositoryException(reason)
        )
    }
}