
package com.example.lab9

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlin.random.Random

data class StoreUiState(
    val instrumentos: List<Instrumento>,
    val marcas: List<Marca>,
    val favoritos: Set<String> = emptySet(),
    val query: String = "",
    val pedido: List<LineaPedido> = emptyList(),
    val mensajePedido: String? = null
)

class StoreViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = StoreDatabase
        .getDatabase(application)
        .storeDao()

    // Lab 11: estado del checkout.
    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState.asStateFlow()

    private val _lastReceipt = MutableStateFlow<OrderReceipt?>(null)

    val lastReceipt: StateFlow<OrderReceipt?> =
        _lastReceipt.asStateFlow()

    private var orderCounter = 0

    fun onFullNameChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            fullName = value,
            isFullNameTouched = true
        )
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            phone = value,
            isPhoneTouched = true
        )
    }

    fun onNitChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            nit = value,
            isNitTouched = true
        )
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            businessName = value,
            isBusinessNameTouched = true
        )
    }

    fun onBillingTypeChange(type: BillingType) {
        val currentState = _checkoutUiState.value

        _checkoutUiState.value = if (type == BillingType.CF) {
            currentState.copy(
                billingType = type,
                isNitTouched = false,
                isBusinessNameTouched = false
            )
        } else {
            currentState.copy(
                billingType = type
            )
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            paymentMethod = method
        )
    }

    fun onConfirmOrder() {
        val checkout = _checkoutUiState.value
        val state = uiState.value

        val units = state.pedido.sumOf { it.cantidad }

        if (!checkout.isFormValid || units <= 0) return

        val total = calcularTotal(
            pedido = state.pedido,
            instrumentos = state.instrumentos
        )

        orderCounter++

        _lastReceipt.value = OrderReceipt(
            folio = "#ORD-${orderCounter.toString().padStart(5, '0')}",
            customerName = checkout.fullName.trim(),
            billingType = checkout.billingType,
            paymentMethod = checkout.paymentMethod,
            total = total
        )

        viewModelScope.launch {
            dao.clearOrderLines()
        }

        _uiState.value = _uiState.value.copy(
            mensajePedido = null
        )

        _checkoutUiState.value = CheckoutUiState()
    }

    private val productosOriginales = listOf(
        Instrumento(
            id = "1",
            nombre = "Guitarra",
            descripcion = "Guitarra acústica",
            precio = 250.0,
            marcaId = "m1",
            stock = 5,
            imageUrl = "https://picsum.photos/seed/1/600/400"
        ),
        Instrumento(
            id = "2",
            nombre = "Piano",
            descripcion = "Piano digital",
            precio = 800.0,
            marcaId = "m2",
            stock = 3,
            imageUrl = "https://picsum.photos/seed/2/600/400"
        ),
        Instrumento(
            id = "3",
            nombre = "Batería",
            descripcion = "Batería acústica",
            precio = 600.0,
            marcaId = "m1",
            stock = 0,
            imageUrl = "https://picsum.photos/seed/3/600/400"
        )
    )

    private val instrumentosGenerados = generarInstrumentos()

    private val _uiState = MutableStateFlow(
        StoreUiState(
            instrumentos = instrumentosGenerados,
            marcas = listOf(
                Marca(
                    id = "m1",
                    nombre = "Yamaha",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion = "Fabricante de instrumentos musicales."
                ),
                Marca(
                    id = "m2",
                    nombre = "Casio",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion = "Fabricante de instrumentos electrónicos."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = combine(
        _uiState,
        dao.observeFavorites(),
        dao.observeOrderLines()
    ) { state, favorites, orderLines ->

        state.copy(
            favoritos = favorites.map {
                it.instrumentoId
            }.toSet(),

            pedido = orderLines.map {
                LineaPedido(
                    instrumentoId = it.instrumentoId,
                    cantidad = it.cantidad
                )
            }
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _uiState.value
    )

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    private fun generarInstrumentos(): List<Instrumento> {
        val random = Random(12345)

        val productos = productosOriginales.toMutableList()

        val nombres = listOf(
            "Guitarra acústica",
            "Guitarra eléctrica",
            "Bajo eléctrico",
            "Teclado digital",
            "Piano digital",
            "Batería acústica",
            "Batería electrónica",
            "Violín",
            "Ukelele",
            "Saxofón",
            "Trompeta",
            "Flauta",
            "Micrófono",
            "Amplificador",
            "Audífonos",
            "Bocina",
            "Pedal de efectos",
            "Atril musical",
            "Cajón peruano",
            "Platillos"
        )

        val preciosMinimos = mapOf(
            "Guitarra acústica" to 200.0,
            "Guitarra eléctrica" to 400.0,
            "Bajo eléctrico" to 450.0,
            "Teclado digital" to 500.0,
            "Piano digital" to 700.0,
            "Batería acústica" to 600.0,
            "Batería electrónica" to 800.0,
            "Violín" to 250.0,
            "Ukelele" to 100.0,
            "Saxofón" to 900.0,
            "Trompeta" to 500.0,
            "Flauta" to 150.0,
            "Micrófono" to 100.0,
            "Amplificador" to 300.0,
            "Audífonos" to 80.0,
            "Bocina" to 150.0,
            "Pedal de efectos" to 120.0,
            "Atril musical" to 70.0,
            "Cajón peruano" to 180.0,
            "Platillos" to 250.0
        )

        val descripciones = mapOf(
            "Guitarra acústica" to
                "Guitarra acústica para práctica y presentaciones.",
            "Guitarra eléctrica" to
                "Guitarra eléctrica para interpretación musical.",
            "Bajo eléctrico" to
                "Bajo eléctrico para interpretación de líneas de bajo.",
            "Teclado digital" to
                "Teclado digital para práctica y producción musical.",
            "Piano digital" to
                "Piano digital con sonidos y funciones electrónicas.",
            "Batería acústica" to
                "Batería acústica para práctica y presentaciones.",
            "Batería electrónica" to
                "Batería electrónica para práctica musical.",
            "Violín" to
                "Violín para estudiantes y músicos.",
            "Ukelele" to
                "Ukelele compacto para práctica y entretenimiento.",
            "Saxofón" to
                "Saxofón para interpretación de música.",
            "Trompeta" to
                "Trompeta para interpretación musical.",
            "Flauta" to
                "Flauta para estudiantes y músicos.",
            "Micrófono" to
                "Micrófono para grabación y presentaciones.",
            "Amplificador" to
                "Amplificador para instrumentos musicales.",
            "Audífonos" to
                "Audífonos para escuchar y monitorear audio.",
            "Bocina" to
                "Bocina para reproducción de audio.",
            "Pedal de efectos" to
                "Pedal para agregar efectos a instrumentos eléctricos.",
            "Atril musical" to
                "Atril para colocar partituras durante la interpretación.",
            "Cajón peruano" to
                "Instrumento de percusión de madera.",
            "Platillos" to
                "Platillos para batería y percusión."
        )

        for (i in 1..497) {
            val nombre = nombres[random.nextInt(nombres.size)]
            val precioMinimo = preciosMinimos[nombre] ?: 100.0
            val precio = precioMinimo + random.nextInt(50, 1000)

            val stock = when (i % 3) {
                0 -> 0
                1 -> 3
                else -> random.nextInt(4, 21)
            }

            val marcaId = if (random.nextBoolean()) {
                "m1"
            } else {
                "m2"
            }

            productos.add(
                Instrumento(
                    id = "generated-$i",
                    nombre = "$nombre $i",
                    descripcion = descripciones[nombre]
                        ?: "Instrumento musical.",
                    precio = precio,
                    marcaId = marcaId,
                    stock = stock,
                    imageUrl =
                        "https://picsum.photos/seed/generated-$i/600/400"
                )
            )
        }

        return productos
    }

    fun alternarFavorito(instrumentoId: String) {
        viewModelScope.launch {
            if (instrumentoId in uiState.value.favoritos) {
                dao.deleteFavorite(instrumentoId)
            } else {
                dao.insertFavorite(
                    FavoriteEntity(instrumentoId)
                )
            }
        }
    }

    fun agregarProducto(
        instrumentoId: String,
        cantidad: Int = 1
    ) {
        viewModelScope.launch {
            val estadoActual = uiState.value

            val instrumento = estadoActual.instrumentos.find {
                it.id == instrumentoId
            } ?: return@launch

            when (
                val resultado = agregarAlPedido(
                    estadoActual.pedido,
                    instrumento,
                    cantidad
                )
            ) {
                is ResultadoPedido.Exito -> {
                    val linea = resultado.pedido.find {
                        it.instrumentoId == instrumentoId
                    } ?: return@launch

                    dao.insertOrderLine(
                        OrderLineEntity(
                            instrumentoId = linea.instrumentoId,
                            cantidad = linea.cantidad
                        )
                    )

                    _uiState.value = _uiState.value.copy(
                        mensajePedido = null
                    )
                }

                is ResultadoPedido.Rechazado -> {
                    _uiState.value = _uiState.value.copy(
                        mensajePedido = resultado.motivo
                    )
                }
            }
        }
    }

    fun disminuirProducto(instrumentoId: String) {
        viewModelScope.launch {
            val estadoActual = uiState.value

            val nuevoPedido = disminuirEnPedido(
                estadoActual.pedido,
                instrumentoId
            )

            val linea = nuevoPedido.find {
                it.instrumentoId == instrumentoId
            }

            if (linea == null) {
                dao.deleteOrderLine(instrumentoId)
            } else {
                dao.insertOrderLine(
                    OrderLineEntity(
                        instrumentoId = linea.instrumentoId,
                        cantidad = linea.cantidad
                    )
                )
            }
        }
    }

    fun eliminarProducto(instrumentoId: String) {
        viewModelScope.launch {
            dao.deleteOrderLine(instrumentoId)
        }
    }
}
