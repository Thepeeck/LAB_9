package com.example.lab9

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
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
                    CatalogoScreen(
                        instrumentos = uiState.instrumentos,
                        favoritos = uiState.favoritos,
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

                is StoreNavKey.Detail -> {
                    val instrumento = uiState.instrumentos.find {
                        it.id == key.productId
                    }

                    if (instrumento != null) {
                        DetalleScreen(
                            instrumento = instrumento,
                            esFavorito = instrumento.id in uiState.favoritos,
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

                is StoreNavKey.Profile -> {
                    val marca = uiState.marcas.find {
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
        }
    )
}
