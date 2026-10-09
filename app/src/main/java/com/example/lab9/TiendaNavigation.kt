package com.example.lab9

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.lab9.Pantalla.CatalogoScreen
import com.example.lab9.Pantalla.DetalleScreen
import com.example.lab9.Pantalla.PerfilScreen
import com.example.lab9.Pantalla.OrderScreen
import androidx.compose.runtime.rememberUpdatedState
import com.example.lab9.Pantalla.CheckoutScreen
import com.example.lab9.Pantalla.OrderConfirmationScreen


@Composable
fun TiendaNavigation(
    uiState: StoreUiState,
    onQueryChange: (String) -> Unit,
    onFavoritoToggle: (String) -> Unit,
    onAgregarProducto: (String) -> Unit,
    onDisminuirProducto: (String) -> Unit,
    onEliminarProducto: (String) -> Unit,
    checkoutUiState: CheckoutUiState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onCatalogOrderChange: (String) -> Unit,
    lastReceipt: OrderReceipt?
) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)
    val currentUiState by rememberUpdatedState(uiState)
    val currentCheckoutState by rememberUpdatedState(checkoutUiState)
    val currentReceipt by rememberUpdatedState(lastReceipt)

    // Vive fuera de NavDisplay para conservar la posición al abrir un detalle.
    val catalogGridState = rememberLazyGridState()

    fun regresar() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        regresar()
    }

    NavDisplay(
        backStack = backStack,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        popTransitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        predictivePopTransitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        entryProvider = { key ->
            NavEntry(key) {
                when (key) {
                    StoreNavKey.Catalog -> {
                        CatalogoScreen(
                            instrumentos = currentUiState.instrumentos,
                            query = currentUiState.query,
                            favoritos = currentUiState.favoritos,
                            gridState = catalogGridState,
                            totalUnidadesPedido = currentUiState.pedido.sumOf { it.cantidad },
                            catalogOrder = currentUiState.catalogOrder,
                            onQueryChange = onQueryChange,
                            onCatalogOrderChange = onCatalogOrderChange,
                            onInstrumentoClick = { instrumentoId ->
                                backStack.add(
                                    StoreNavKey.Detail(
                                        productId = instrumentoId
                                    )
                                )
                            },
                            onFavoritoToggle = onFavoritoToggle,
                            onVerPedido = {
                                backStack.add(StoreNavKey.Order)
                            }
                        )
                    }

                    is StoreNavKey.Detail -> {
                        val instrumento = currentUiState.instrumentos.find {
                            it.id == key.productId
                        }

                        if (instrumento != null) {
                            DetalleScreen(
                                instrumento = instrumento,
                                esFavorito = instrumento.id in currentUiState.favoritos,
                                cantidadEnPedido = currentUiState.pedido.find{
                                    it.instrumentoId == instrumento.id
                                }?.cantidad ?: 0,
                                mensajePedido = currentUiState.mensajePedido,
                                onFavoritoToggle = onFavoritoToggle,
                                onAgregarPedido = onAgregarProducto,
                                onVerPerfil = { marcaId ->
                                    backStack.add(
                                        StoreNavKey.Profile(
                                            profileId = marcaId
                                        )
                                    )
                                },
                                onBack = {
                                    regresar()
                                }
                            )

                        }
                    }

                    is StoreNavKey.Profile -> {
                        val marca = currentUiState.marcas.find {
                            it.id == key.profileId
                        }

                        if (marca != null) {
                            PerfilScreen(
                                marca = marca,
                                onBack = {
                                    regresar()
                                }
                            )
                        }
                    }

                    StoreNavKey.Order -> {
                        OrderScreen(
                            pedido = currentUiState.pedido,
                            instrumentos = currentUiState.instrumentos,
                            onDisminuir = onDisminuirProducto,
                            onEliminar = onEliminarProducto,
                            onContinuarCheckout = {
                                backStack.add(StoreNavKey.Checkout)
                            },
                            onBack = { regresar() }
                        )
                    }
                    StoreNavKey.Checkout -> {
                        CheckoutScreen(
                            uiState = currentCheckoutState,
                            pedido = currentUiState.pedido,
                            instrumentos = currentUiState.instrumentos,
                            onFullNameChange = onFullNameChange,
                            onPhoneChange = onPhoneChange,
                            onBillingTypeChange = onBillingTypeChange,
                            onNitChange = onNitChange,
                            onBusinessNameChange = onBusinessNameChange,
                            onPaymentMethodChange = onPaymentMethodChange,
                            onConfirmOrder = {
                                onConfirmOrder()
                                backStack.add(StoreNavKey.OrderConfirmation)
                            },
                            onBack = { regresar() }
                        )
                    }

                    StoreNavKey.OrderConfirmation -> {
                        currentReceipt?.let { receipt ->
                            OrderConfirmationScreen(
                                receipt = receipt,
                                onBackToCatalog = {
                                    while (backStack.size > 1) {
                                        backStack.removeLastOrNull()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}