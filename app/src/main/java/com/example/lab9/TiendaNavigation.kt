package com.example.lab9

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.lab9.Pantalla.CatalogStateContent
import com.example.lab9.Pantalla.CatalogoScreen
import com.example.lab9.Pantalla.CheckoutScreen
import com.example.lab9.Pantalla.DetalleScreen
import com.example.lab9.Pantalla.OrderConfirmationScreen
import com.example.lab9.Pantalla.OrderDetailScreen
import com.example.lab9.Pantalla.OrderScreen
import com.example.lab9.Pantalla.OrdersScreen
import com.example.lab9.Pantalla.PerfilScreen

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
    orderSubmissionState: OrderSubmissionState,
    onOrderConfirmationShown: () -> Unit,
    onCatalogOrderChange: (String) -> Unit,
    lastReceipt: OrderReceipt?,
    ordersUiState: OrdersUiState,
    orderDetailUiState: OrderDetailUiState,
    onLoadOrders: (Boolean) -> Unit,
    onLoadOrderDetail: (String, Boolean) -> Unit,
    onRetryCatalog: () -> Unit
) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)

    val currentUiState by rememberUpdatedState(uiState)
    val currentCheckoutState by rememberUpdatedState(checkoutUiState)
    val currentReceipt by rememberUpdatedState(lastReceipt)
    val currentSubmissionState by rememberUpdatedState(orderSubmissionState)
    val currentOrdersState by rememberUpdatedState(ordersUiState)
    val currentOrderDetailState by rememberUpdatedState(orderDetailUiState)

    val catalogGridState = rememberLazyGridState()

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun returnToCatalog() {
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
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
                        CatalogStateContent(
                            state = currentUiState.catalogState,
                            onRetry = onRetryCatalog
                        ) {
                            CatalogoScreen(
                                instrumentos = currentUiState.instrumentos,
                                query = currentUiState.query,
                                favoritos = currentUiState.favoritos,
                                gridState = catalogGridState,
                                totalUnidadesPedido =
                                    currentUiState.pedido.sumOf { line ->
                                        line.cantidad
                                    },
                                catalogOrder = currentUiState.catalogOrder,
                                onQueryChange = onQueryChange,
                                onCatalogOrderChange = onCatalogOrderChange,
                                onInstrumentoClick = { productId ->
                                    backStack.add(
                                        StoreNavKey.Detail(
                                            productId = productId
                                        )
                                    )
                                },
                                onFavoritoToggle = onFavoritoToggle,
                                onVerPedido = {
                                    backStack.add(StoreNavKey.Order)
                                },
                                onVerMisPedidos = {
                                    onLoadOrders(true)
                                    backStack.add(StoreNavKey.Orders)
                                }
                            )
                        }
                    }

                    is StoreNavKey.Detail -> {
                        val instrument =
                            currentUiState.instrumentos.find { product ->
                                product.id == key.productId
                            }

                        if (instrument != null) {
                            DetalleScreen(
                                instrumento = instrument,
                                esFavorito =
                                    instrument.id in currentUiState.favoritos,
                                cantidadEnPedido =
                                    currentUiState.pedido.find { line ->
                                        line.instrumentoId == instrument.id
                                    }?.cantidad ?: 0,
                                mensajePedido = currentUiState.mensajePedido,
                                onFavoritoToggle = onFavoritoToggle,
                                onAgregarPedido = onAgregarProducto,
                                onVerPerfil = { brandId ->
                                    backStack.add(
                                        StoreNavKey.Profile(
                                            profileId = brandId
                                        )
                                    )
                                },
                                onBack = {
                                    navigateBack()
                                }
                            )
                        }
                    }

                    is StoreNavKey.Profile -> {
                        val brand = currentUiState.marcas.find { item ->
                            item.id == key.profileId
                        }

                        if (brand != null) {
                            PerfilScreen(
                                marca = brand,
                                onBack = {
                                    navigateBack()
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
                            onBack = {
                                navigateBack()
                            }
                        )
                    }

                    StoreNavKey.Checkout -> {
                        LaunchedEffect(currentSubmissionState) {
                            if (
                                currentSubmissionState ==
                                OrderSubmissionState.Success
                            ) {
                                onOrderConfirmationShown()

                                backStack.add(
                                    StoreNavKey.OrderConfirmation
                                )
                            }
                        }

                        CheckoutScreen(
                            uiState = currentCheckoutState,
                            submissionState = currentSubmissionState,
                            pedido = currentUiState.pedido,
                            instrumentos = currentUiState.instrumentos,
                            onFullNameChange = onFullNameChange,
                            onPhoneChange = onPhoneChange,
                            onBillingTypeChange = onBillingTypeChange,
                            onNitChange = onNitChange,
                            onBusinessNameChange = onBusinessNameChange,
                            onPaymentMethodChange = onPaymentMethodChange,
                            onConfirmOrder = onConfirmOrder,
                            onBack = {
                                navigateBack()
                            }
                        )
                    }

                    StoreNavKey.OrderConfirmation -> {
                        currentReceipt?.let { receipt ->
                            OrderConfirmationScreen(
                                receipt = receipt,
                                onBackToCatalog = {
                                    returnToCatalog()
                                }
                            )
                        }
                    }

                    StoreNavKey.Orders -> {
                        OrdersScreen(
                            state = currentOrdersState,
                            onOrderClick = { orderId ->
                                onLoadOrderDetail(orderId, true)

                                backStack.add(
                                    StoreNavKey.OrderDetail(orderId)
                                )
                            },
                            onRetry = {
                                onLoadOrders(true)
                            },
                            onBack = {
                                navigateBack()
                            },
                            onCatalog = {
                                returnToCatalog()
                            }
                        )
                    }

                    is StoreNavKey.OrderDetail -> {
                        OrderDetailScreen(
                            state = currentOrderDetailState,
                            onBack = {
                                navigateBack()
                            },
                            onRetry = {
                                onLoadOrderDetail(key.orderId, true)
                            }
                        )
                    }
                }
            }
        }
    )
}