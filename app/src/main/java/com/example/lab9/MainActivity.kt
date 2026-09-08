package com.example.lab9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.lab9.Pantalla.CatalogoScreen
import com.example.lab9.Pantalla.DetalleScreen
import com.example.lab9.Pantalla.PerfilScreen
import com.example.lab9.ui.theme.LAB9Theme

class MainActivity : ComponentActivity() {

    private val storeViewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LAB9Theme {
                TiendaApp(storeViewModel)
            }
        }
    }
}

@Composable
fun TiendaApp(viewModel: StoreViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(StoreNavKey.Catalog)

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
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
                                StoreNavKey.Detail(instrumentoId)
                            )
                        },

                        onFavoritoToggle = { instrumentoId ->
                            viewModel.alternarFavorito(instrumentoId)
                        }
                    )
                }

                is StoreNavKey.Detail -> {

                    val instrumento =
                        uiState.instrumentos.find {
                            it.id == key.productId
                        }

                    if (instrumento != null) {
                        DetalleScreen(
                            instrumento = instrumento,
                            esFavorito =
                                instrumento.id in uiState.favoritos,

                            onFavoritoToggle = { instrumentoId ->
                                viewModel.alternarFavorito(instrumentoId)
                            },

                            onVerPerfil = { marcaId ->
                                backStack.add(
                                    StoreNavKey.Profile(marcaId)
                                )
                            },

                            onBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                }

                is StoreNavKey.Profile -> {

                    val marca =
                        uiState.marcas.find {
                            it.id == key.profileId
                        }

                    if (marca != null) {
                        PerfilScreen(
                            marca = marca,
                            onBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                }
            }
        }
    )
}