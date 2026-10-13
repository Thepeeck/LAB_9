
package com.example.lab9

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

sealed interface OrdersUiState {
    data object Idle : OrdersUiState
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Success(val orders: List<OrderDto>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

sealed interface OrderDetailUiState {
    data object Idle : OrderDetailUiState
    data object Loading : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Success(val order: OrderDto) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

class OrdersViewModel : ViewModel() {

    private val api = OrderApiClient.api

    private val _ordersUiState =
        MutableStateFlow<OrdersUiState>(OrdersUiState.Idle)

    val ordersUiState: StateFlow<OrdersUiState> =
        _ordersUiState.asStateFlow()

    private val _detailUiState =
        MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Idle)

    val detailUiState: StateFlow<OrderDetailUiState> =
        _detailUiState.asStateFlow()

    private var loadedOrderId: String? = null

    fun loadOrders(force: Boolean = false) {
        if (!force && _ordersUiState.value != OrdersUiState.Idle) return
        if (_ordersUiState.value == OrdersUiState.Loading) return

        viewModelScope.launch {
            _ordersUiState.value = OrdersUiState.Loading

            _ordersUiState.value = try {
                val orders = api.getOrders(
                    sortBy = "createdAt",
                    order = "desc"
                )

                if (orders.isEmpty()) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Success(orders)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                OrdersUiState.Error(
                    "Revisa tu conexión e intenta de nuevo."
                )
            } catch (e: HttpException) {
                OrdersUiState.Error(
                    "No pudimos consultar tus pedidos."
                )
            } catch (e: SerializationException) {
                OrdersUiState.Error(
                    "Recibimos una respuesta inesperada."
                )
            }
        }
    }

    fun loadOrder(id: String, force: Boolean = false) {
        if (!force && loadedOrderId == id &&
            _detailUiState.value != OrderDetailUiState.Idle
        ) return

        if (_detailUiState.value == OrderDetailUiState.Loading) return

        loadedOrderId = id

        viewModelScope.launch {
            _detailUiState.value = OrderDetailUiState.Loading

            _detailUiState.value = try {
                OrderDetailUiState.Success(api.getOrder(id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                OrderDetailUiState.Error(
                    "Revisa tu conexión e intenta de nuevo."
                )
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    OrderDetailUiState.NotFound
                } else {
                    OrderDetailUiState.Error(
                        "No pudimos consultar este pedido."
                    )
                }
            } catch (e: SerializationException) {
                OrderDetailUiState.Error(
                    "Recibimos una respuesta inesperada."
                )
            }
        }
    }
}
