package com.example.lab9

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.lab9.Pantalla.CatalogoScreen
import com.example.lab9.Pantalla.DetalleScreen
import com.example.lab9.Pantalla.PerfilScreen

@Composable
fun TiendaNavigation(
    uiState: StoreUiState,
    onFavoritoToggle: (String) -> Unit
) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)
    val estadoActual by rememberUpdatedState(uiState)

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
        entryProvider = { key ->
            when (key) {
                StoreNavKey.Catalog -> {
                    NavEntry(key) {
                        CatalogoScreen(
                            instrumentos = estadoActual.instrumentos,
                            favoritos = estadoActual.favoritos,
                            onInstrumentoClick = { instrumentoId ->
                                backStack.add(
                                    StoreNavKey.Detail(
                                        productId = instrumentoId
                                    )
                                )
                            },
                            onFavoritoToggle = onFavoritoToggle
                        )
                    }
                }

                is StoreNavKey.Detail -> {
                    NavEntry(key) {
                        val instrumento = estadoActual.instrumentos.find {
                            it.id == key.productId
                        }

                        if (instrumento != null) {
                            DetalleScreen(
                                instrumento = instrumento,
                                esFavorito = instrumento.id in estadoActual.favoritos,
                                onFavoritoToggle = onFavoritoToggle,
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
                }

                is StoreNavKey.Profile -> {
                    NavEntry(key) {
                        val marca = estadoActual.marcas.find {
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
                }

                else -> error("Ruta de navegación no reconocida: $key")
            }
        }
    )
}