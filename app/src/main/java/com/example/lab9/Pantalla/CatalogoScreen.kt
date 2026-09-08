package com.example.lab9.Pantalla

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab9.Instrumento
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen (
    instrumentos: List<Instrumento>,
    favoritos: Set<String>,
    onInstrumentoClick: (String) -> Unit,
    onFavoritoToggle: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("catalogo de instrumentos") })
        }
    ) { padding ->
        Column( modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
        ) {
            instrumentos.forEach { instrumento ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInstrumentoClick (instrumento.id)}
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(modifier = Modifier.weight(1f)) {
                        Text(instrumento.nombre)
                        Text("$${instrumento.precio}")
                    }
                    IconButton(onClick = { onFavoritoToggle(instrumento.id)}) {
                        Icon(
                            imageVector = if (favoritos.contains(instrumento.id)){
                                Icons.Filled.Favorite
                            }else{
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