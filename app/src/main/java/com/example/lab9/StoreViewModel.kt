package com.example.lab9

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class StoreUiState(
    val instrumentos: List<Instrumento>,
    val marcas: List<Marca>,
    val favoritos: Set<String> = emptySet(),
    val query: String = "",
    val pedido: List<LineaPedido> = emptyList(),
    val mensajePedido: String? = null,
    val catalogOrder: String = "name",
    val catalogState: CatalogUiState = CatalogUiState.Loading
)

class StoreViewModel(
    private val productRepository: ProductRepository,
    private val favoritesRepository: FavoritesRepository,
    private val cartRepository: CartRepository,
    private val preferencesRepository: CatalogPreferencesRepository,
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _isReserving = MutableStateFlow(false)
    val isReserving: StateFlow<Boolean> =
        _isReserving.asStateFlow()

    private val _concurrencyTestResult =
        MutableStateFlow<String?>(null)

    val concurrencyTestResult: StateFlow<String?> =
        _concurrencyTestResult.asStateFlow()

    private val cartMutex = Mutex()
    private val favoritesMutex = Mutex()

    private var catalogJob: Job? = null
    private var pendingReceipt: OrderReceipt? = null

    private val _checkoutUiState =
        MutableStateFlow(CheckoutUiState())

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState.asStateFlow()

    private val _lastReceipt =
        MutableStateFlow<OrderReceipt?>(null)

    val lastReceipt: StateFlow<OrderReceipt?> =
        _lastReceipt.asStateFlow()

    private val _orderSubmissionState =
        MutableStateFlow<OrderSubmissionState>(
            OrderSubmissionState.Idle
        )

    val orderSubmissionState: StateFlow<OrderSubmissionState> =
        _orderSubmissionState.asStateFlow()

    private val _uiState = MutableStateFlow(
        StoreUiState(
            instrumentos = emptyList(),
            marcas = listOf(
                Marca(
                    id = "m1",
                    nombre = "Yamaha",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion =
                        "Fabricante de instrumentos musicales."
                ),
                Marca(
                    id = "m2",
                    nombre = "Casio",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion =
                        "Fabricante de instrumentos electrónicos."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = combine(
        _uiState,
        favoritesRepository.observeFavoriteIds(),
        cartRepository.observeCart(),
        preferencesRepository.observeCatalogOrder()
    ) { state, favoriteIds, cart, order ->
        state.copy(
            instrumentos = ordenarInstrumentos(
                state.instrumentos,
                order
            ),
            favoritos = favoriteIds,
            pedido = cart,
            catalogOrder = order
        )
    }.catch { exception ->
        if (exception is CancellationException) {
            throw exception
        }

        showMessage(
            "No pudimos leer los datos locales. " +
                "Cierra y vuelve a abrir la aplicación."
        )

        emit(_uiState.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _uiState.value
    )

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        if (catalogJob?.isActive == true) {
            return
        }

        _uiState.update { current ->
            current.copy(
                catalogState = CatalogUiState.Loading
            )
        }

        catalogJob = viewModelScope.launch {
            productRepository.observeProducts()
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update { current ->
                        current.copy(
                            catalogState = CatalogUiState.Error(
                                "No pudimos cargar el catálogo. " +
                                    "Revisa tu conexión e intenta de nuevo."
                            )
                        )
                    }
                }
                .collect { products ->
                    val instruments = products.map { product ->
                        Instrumento(
                            id = product.id,
                            nombre = product.name,
                            descripcion = product.description,
                            precio = product.price,
                            marcaId = product.brandId,
                            stock = product.stock,
                            imageUrl = product.imageUrl
                        )
                    }

                    _uiState.update { current ->
                        current.copy(
                            instrumentos = instruments,
                            catalogState = CatalogUiState.Success
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(query = query)
        }
    }

    fun onCatalogOrderChange(order: String) {
        viewModelScope.launch {
            val result =
                preferencesRepository.setCatalogOrder(order)

            if (result.isFailure) {
                showMessage(
                    "No pudimos guardar la preferencia de orden."
                )
            }
        }
    }

    fun onFullNameChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                fullName = value,
                isFullNameTouched = true
            )
        }
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                phone = value,
                isPhoneTouched = true
            )
        }
    }

    fun onNitChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                nit = value,
                isNitTouched = true
            )
        }
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                businessName = value,
                isBusinessNameTouched = true
            )
        }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutUiState.update { current ->
            if (type == BillingType.CF) {
                current.copy(
                    billingType = type,
                    isNitTouched = false,
                    isBusinessNameTouched = false
                )
            } else {
                current.copy(billingType = type)
            }
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutUiState.update { current ->
            current.copy(paymentMethod = method)
        }
    }

    fun onOrderConfirmationShown() {
        if (
            _orderSubmissionState.value ==
            OrderSubmissionState.Success
        ) {
            _orderSubmissionState.value =
                OrderSubmissionState.Idle
        }
    }

    fun onConfirmOrder() {
        if (
            _orderSubmissionState.value ==
            OrderSubmissionState.Loading
        ) {
            return
        }

        val checkout = _checkoutUiState.value

        if (pendingReceipt == null && !checkout.isFormValid) {
            return
        }

        _orderSubmissionState.value =
            OrderSubmissionState.Loading

        viewModelScope.launch {
            cartMutex.withLock {
                val receipt = pendingReceipt

                if (receipt != null) {
                    completeSubmittedOrder(receipt)
                    return@withLock
                }

                val cartResult = captureRepositoryResult {
                    cartRepository.observeCart().first()
                }

                val cart = cartResult.getOrNull()

                if (cart == null) {
                    submissionError(
                        "No pudimos leer tu pedido. Intenta de nuevo."
                    )
                    return@withLock
                }

                if (cart.isEmpty()) {
                    submissionError(
                        "Agrega productos antes de confirmar."
                    )
                    return@withLock
                }

                val productsById =
                    _uiState.value.instrumentos.associateBy {
                        product -> product.id
                    }

                if (cart.any {
                    it.instrumentoId !in productsById
                }) {
                    submissionError(
                        "No pudimos encontrar todos los productos. " +
                            "Revisa el catálogo antes de confirmar."
                    )
                    return@withLock
                }

                val lines = cart.map { line ->
                    val product = productsById.getValue(
                        line.instrumentoId
                    )

                    OrderLine(
                        productId = product.id,
                        name = product.nombre,
                        unitPrice = product.precio,
                        quantity = line.cantidad
                    )
                }

                val draft = OrderDraft(
                    lines = lines,
                    total = lines.sumOf { line ->
                        line.unitPrice * line.quantity
                    },
                    createdAt = Instant.now().toString()
                )

                val result = ordersRepository.submitOrder(draft)
                val savedOrder = result.getOrNull()

                if (savedOrder == null) {
                    submissionError(
                        orderErrorMessage(
                            result.exceptionOrNull()
                        )
                    )
                    return@withLock
                }

                val savedReceipt = OrderReceipt(
                    folio = "#${savedOrder.id}",
                    customerName = checkout.fullName.trim(),
                    billingType = checkout.billingType,
                    paymentMethod = checkout.paymentMethod,
                    total = savedOrder.total
                )

                pendingReceipt = savedReceipt

                completeSubmittedOrder(savedReceipt)
            }
        }
    }

    private suspend fun completeSubmittedOrder(
        receipt: OrderReceipt
    ) {
        val result = cartRepository.clearCart()

        if (result.isFailure) {
            submissionError(
                "Tu pedido ya fue enviado, pero no pudimos " +
                    "vaciar el carrito. Reintenta para finalizar."
            )
            return
        }

        _lastReceipt.value = receipt
        pendingReceipt = null
        _checkoutUiState.value = CheckoutUiState()
        showMessage(null)

        _orderSubmissionState.value =
            OrderSubmissionState.Success
    }

    fun alternarFavorito(instrumentoId: String) {
        viewModelScope.launch {
            favoritesMutex.withLock {
                val currentResult = captureRepositoryResult {
                    favoritesRepository.observeFavoriteIds().first()
                }

                val favoriteIds = currentResult.getOrNull()

                if (favoriteIds == null) {
                    showMessage(
                        "No pudimos consultar tus favoritos."
                    )
                    return@withLock
                }

                val result = favoritesRepository.setFavorite(
                    productId = instrumentoId,
                    isFavorite = instrumentoId !in favoriteIds
                )

                if (result.isFailure) {
                    showMessage(
                        "No pudimos actualizar tus favoritos."
                    )
                }
            }
        }
    }

    fun agregarProducto(
        instrumentoId: String,
        cantidad: Int = 1
    ) {
        if (isCartLocked() || cantidad <= 0) return

        viewModelScope.launch {
            cartMutex.withLock {
                if (isCartLocked()) return@withLock

                _isReserving.value = true

                try {
                    val product =
                        _uiState.value.instrumentos.find {
                            it.id == instrumentoId
                        } ?: return@withLock

                    val cart = readCart() ?: return@withLock

                    val check = agregarAlPedido(
                        cart,
                        product,
                        cantidad
                    )

                    if (check is ResultadoPedido.Rechazado) {
                        showMessage(check.motivo)
                        return@withLock
                    }

                    val current = cart.find {
                        it.instrumentoId == instrumentoId
                    }?.cantidad ?: 0

                    var reserved = 0

                    for (attempt in 1..cantidad) {
                        if (
                            productRepository.reserveUnit(
                                instrumentoId
                            ).isFailure
                        ) {
                            break
                        }
                        reserved++
                    }

                    if (reserved != cantidad) {
                        val reverted = if (reserved > 0) {
                            productRepository.releaseUnits(
                                instrumentoId,
                                reserved
                            )
                        } else {
                            Result.success(Unit)
                        }

                        showMessage(
                            if (reverted.isSuccess) {
                                "No hay suficientes existencias."
                            } else {
                                "Reserva incompleta: no se pudieron " +
                                    "devolver todas las unidades."
                            }
                        )
                        return@withLock
                    }

                    val saved = cartRepository.setQuantity(
                        instrumentoId,
                        current + cantidad
                    )

                    if (saved.isFailure) {
                        val reverted =
                            productRepository.releaseUnits(
                                instrumentoId,
                                cantidad
                            )

                        showMessage(
                            if (reverted.isSuccess) {
                                "No pudimos guardar el pedido."
                            } else {
                                "Error de sincronización. No vuelvas " +
                                    "a reservar hasta revisar las existencias."
                            }
                        )
                    } else {
                        showMessage(null)
                    }
                } finally {
                    _isReserving.value = false
                }
            }
        }
    }

    fun disminuirProducto(instrumentoId: String) {
        changeCartQuantity(
            instrumentoId,
            removeAll = false
        )
    }

    fun eliminarProducto(instrumentoId: String) {
        changeCartQuantity(
            instrumentoId,
            removeAll = true
        )
    }

    private fun changeCartQuantity(
        instrumentoId: String,
        removeAll: Boolean
    ) {
        if (isCartLocked()) return

        viewModelScope.launch {
            cartMutex.withLock {
                if (isCartLocked()) return@withLock

                _isReserving.value = true

                try {
                    val cart = readCart() ?: return@withLock

                    val line = cart.find {
                        it.instrumentoId == instrumentoId
                    } ?: return@withLock

                    val amount = if (removeAll) {
                        line.cantidad
                    } else {
                        1
                    }

                    val released =
                        productRepository.releaseUnits(
                            instrumentoId,
                            amount
                        )

                    if (released.isFailure) {
                        showMessage(
                            "No pudimos devolver las existencias."
                        )
                        return@withLock
                    }

                    val updated =
                        if (line.cantidad == amount) {
                            cartRepository.removeProduct(
                                instrumentoId
                            )
                        } else {
                            cartRepository.setQuantity(
                                instrumentoId,
                                line.cantidad - amount
                            )
                        }

                    if (updated.isFailure) {
                        var restored = 0

                        repeat(amount) {
                            if (
                                productRepository.reserveUnit(
                                    instrumentoId
                                ).isSuccess
                            ) {
                                restored++
                            }
                        }

                        showMessage(
                            if (restored == amount) {
                                "No pudimos actualizar el carrito."
                            } else {
                                "Error de sincronización. Revisa " +
                                    "el pedido y las existencias."
                            }
                        )
                    } else {
                        showMessage(null)
                    }
                } finally {
                    _isReserving.value = false
                }
            }
        }
    }

    fun onConcurrencyTest(
        productId: String,
        attempts: Int = 20
    ) {
        if (isCartLocked() || attempts <= 0) return

        viewModelScope.launch {
            cartMutex.withLock {
                if (isCartLocked()) return@withLock

                _isReserving.value = true
                _concurrencyTestResult.value =
                    "Probando reservas..."

                try {
                    val results = List(attempts) {
                        async {
                            productRepository.reserveUnit(
                                productId
                            )
                        }
                    }.awaitAll()

                    val successes = results.count {
                        it.isSuccess
                    }

                    _concurrencyTestResult.value =
                        "Éxitos: $successes · " +
                            "Rechazos: ${attempts - successes}"
                } finally {
                    _isReserving.value = false
                }
            }
        }
    }

    private suspend fun readCart(): List<LineaPedido>? {
        val result = captureRepositoryResult {
            cartRepository.observeCart().first()
        }

        if (result.isFailure) {
            showMessage("No pudimos leer tu pedido.")
        }

        return result.getOrNull()
    }

    private fun isCartLocked(): Boolean {
        return _isReserving.value ||
            _orderSubmissionState.value ==
                OrderSubmissionState.Loading ||
            pendingReceipt != null
    }

    private fun showMessage(message: String?) {
        _uiState.update { current ->
            current.copy(mensajePedido = message)
        }
    }

    private fun submissionError(message: String) {
        _orderSubmissionState.value =
            OrderSubmissionState.Error(message)
    }

    private fun orderErrorMessage(
        exception: Throwable?
    ): String {
        val reason =
            (exception as? OrderRepositoryException)?.reason

        return when (reason) {
            OrderFailureReason.CONNECTION ->
                "No pudimos enviar tu pedido. " +
                    "Revisa tu conexión e intenta de nuevo."

            OrderFailureReason.NOT_FOUND,
            OrderFailureReason.SERVER ->
                "El servidor no pudo atender la solicitud."

            OrderFailureReason.INVALID_RESPONSE ->
                "Recibimos una respuesta inesperada."

            null ->
                "No pudimos enviar tu pedido. Intenta de nuevo."
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(
                        this[
                            ViewModelProvider.AndroidViewModelFactory
                                .APPLICATION_KEY
                        ]
                    ) as StoreApplication

                    val container = application.container

                    StoreViewModel(
                        productRepository =
                            container.productRepository,
                        favoritesRepository =
                            container.favoritesRepository,
                        cartRepository =
                            container.cartRepository,
                        preferencesRepository =
                            container.catalogPreferencesRepository,
                        ordersRepository =
                            container.ordersRepository
                    )
                }
            }
    }
}
