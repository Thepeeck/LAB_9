package com.example.lab9

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCartRepository(
    private val dao: StoreDao
) : CartRepository {

    override fun observeCart(): Flow<List<LineaPedido>> {
        return dao.observeOrderLines().map { entities ->
            entities.map { entity ->
                LineaPedido(
                    instrumentoId = entity.instrumentoId,
                    cantidad = entity.cantidad
                )
            }
        }
    }

    override suspend fun setQuantity(
        productId: String,
        quantity: Int
    ): Result<Unit> {
        return captureRepositoryResult {
            require(quantity > 0) {
                "Quantity must be greater than zero."
            }

            dao.insertOrderLine(
                OrderLineEntity(
                    instrumentoId = productId,
                    cantidad = quantity
                )
            )
        }
    }

    override suspend fun removeProduct(
        productId: String
    ): Result<Unit> {
        return captureRepositoryResult {
            dao.deleteOrderLine(productId)
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return captureRepositoryResult {
            dao.clearOrderLines()
        }
    }
}