package com.example.lab9.Pantalla

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab9.Instrumento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    instrumentos: List<Instrumento>,
    favoritos: Set<String>,
    onInstrumentoClick: (String) -> Unit,
    onFavoritoToggle: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("catalogo de instrumentos")
                }
            )
        }
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(8.dp)
        ) {

            items(
                items = instrumentos,
                key = { instrumento -> instrumento.id }
            ) { instrumento ->

                DisposableEffect(instrumento.id) {
                    Log.d(
                        "Catalogo",
                        "Entró: ${instrumento.id}"
                    )

                    onDispose {
                        Log.d(
                            "Catalogo",
                            "Salió: ${instrumento.id}"
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            onInstrumentoClick(instrumento.id)
                        }
                ) {

                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                    ) {

                        Text(instrumento.nombre)

                        Text(
                            "$${instrumento.precio}"
                        )

                        Text(
                            "Stock: ${instrumento.stock}"
                        )

                        IconButton(
                            onClick = {
                                onFavoritoToggle(instrumento.id)
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (favoritos.contains(instrumento.id)) {
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