package com.example.lab9

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface OrdersUiState {
    data object Idle : OrdersUiState
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Success(val orders: List<Order>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

sealed interface OrderDetailUiState {
    data object Idle : OrderDetailUiState
    data object Loading : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Success(val order: Order) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

class OrdersViewModel(
    private val repository: OrdersRepository
) : ViewModel() {

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
        if (_ordersUiState.value == OrdersUiState.Loading) {
            return
        }

        if (!force && _ordersUiState.value != OrdersUiState.Idle) {
            return
        }

        _ordersUiState.value = OrdersUiState.Loading

        viewModelScope.launch {
            val result = repository.refreshOrders()

            if (result.isSuccess) {
                val orders = repository.observeOrders().first()

                _ordersUiState.value = if (orders.isEmpty()) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Success(orders)
                }
            } else {
                _ordersUiState.value = OrdersUiState.Error(
                    message = errorMessage(result.exceptionOrNull())
                )
            }
        }
    }

    fun loadOrder(
        id: String,
        force: Boolean = false
    ) {
        if (_detailUiState.value == OrderDetailUiState.Loading) {
            return
        }

        if (
            !force &&
            loadedOrderId == id &&
            _detailUiState.value != OrderDetailUiState.Idle
        ) {
            return
        }

        loadedOrderId = id
        _detailUiState.value = OrderDetailUiState.Loading

        viewModelScope.launch {
            val result = repository.refreshOrder(id)

            if (result.isSuccess) {
                val order = repository.observeOrder(id).first()

                _detailUiState.value = if (order != null) {
                    OrderDetailUiState.Success(order)
                } else {
                    OrderDetailUiState.NotFound
                }
            } else {
                val exception = result.exceptionOrNull()

                val reason =
                    (exception as? OrderRepositoryException)?.reason

                _detailUiState.value =
                    if (reason == OrderFailureReason.NOT_FOUND) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            message = errorMessage(exception)
                        )
                    }
            }
        }
    }

    private fun errorMessage(
        exception: Throwable?
    ): String {
        val reason =
            (exception as? OrderRepositoryException)?.reason

        return when (reason) {
            OrderFailureReason.CONNECTION ->
                "Revisa tu conexión e intenta de nuevo."

            OrderFailureReason.NOT_FOUND ->
                "No encontramos los datos solicitados."

            OrderFailureReason.SERVER ->
                "El servidor no pudo atender la solicitud."

            OrderFailureReason.INVALID_RESPONSE ->
                "Recibimos una respuesta inesperada."

            null ->
                "No pudimos completar la operación. Intenta de nuevo."
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[
                        ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                    ]
                ) as StoreApplication

                OrdersViewModel(
                    repository = application.container.ordersRepository
                )
            }
        }
    }
}
