package com.example.lab9.Pantalla

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab9.Instrumento
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    instrumentos: List<Instrumento>,
    query: String,
    favoritos: Set<String>,
    totalUnidadesPedido: Int,
    onQueryChange: (String) -> Unit,
    onInstrumentoClick: (String) -> Unit,
    onFavoritoToggle: (String) -> Unit,
    onVerPedido: () -> Unit
) {
    val filteredProducts = remember(instrumentos, query) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.isEmpty()) {
            instrumentos
        } else {
            instrumentos.filter { instrumento ->
                instrumento.nombre.contains(
                    other = normalizedQuery,
                    ignoreCase = true
                )
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Catálogo de instrumentos (convencional)")
                },
                actions = {
                    TextButton(onClick = onVerPedido) {
                        Text("Pedido · $totalUnidadesPedido")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { newQuery ->
                    onQueryChange(newQuery)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = {
                    Text("Buscar productos")
                },
                singleLine = true
            )

            Text(
                text = "${filteredProducts.size} de " +
                        "${instrumentos.size} productos",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No encontramos productos.")

                    Button(
                        onClick = {
                            onQueryChange("")
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Limpiar búsqueda")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredProducts.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fila.forEach { instrumento ->
                                DisposableEffect(instrumento.id) {
                                    Log.d(
                                        "CatalogoProbe",
                                        "Entró: ${instrumento.id}"
                                    )

                                    onDispose {
                                        Log.d(
                                            "CatalogoProbe",
                                            "Salió: ${instrumento.id}"
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clickable {
                                            onInstrumentoClick(
                                                instrumento.id
                                            )
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(instrumento.nombre)
                                        Text("$${instrumento.precio}")
                                        Text("Stock: ${instrumento.stock}")

                                        IconButton(
                                            onClick = {
                                                onFavoritoToggle(
                                                    instrumento.id
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector =
                                                    if (
                                                        instrumento.id in favoritos
                                                    ) {
                                                        Icons.Filled.Favorite
                                                    } else {
                                                        Icons.Filled.FavoriteBorder
                                                    },
                                                contentDescription = "Favorito"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}