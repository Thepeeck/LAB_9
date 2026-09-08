package com.example.lab9.Pantalla

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.lab9.Instrumento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    instrumento: Instrumento,
    esFavorito: Boolean,
    onFavoritoToggle: (String) -> Unit,
    onVerPerfil: (String) -> Unit,
    onBack: () -> Unit
) {
    var mostrarFichaTecnica by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(instrumento.nombre) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(instrumento.nombre)
            Text("$${instrumento.precio}")
            Text(instrumento.descripcion)

            IconButton(onClick = { onFavoritoToggle(instrumento.id) }) {
                Icon(
                    imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorito"
                )
            }

            TextButton(onClick = { mostrarFichaTecnica = !mostrarFichaTecnica }) {
                Text(if (mostrarFichaTecnica) "Ocultar ficha técnica" else "Ver más")
            }

            if (mostrarFichaTecnica) {
                Text("Ficha técnica de ${instrumento.nombre}: ${instrumento.descripcion}")
            }

            TextButton(onClick = { onVerPerfil(instrumento.marcaId) }) {
                Text("Ver marca")
            }
        }
    }
}