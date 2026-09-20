package com.example.lab9.Pantalla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.example.lab9.LineaPedido
import com.example.lab9.calcularSubtotal
import com.example.lab9.calcularTotal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    pedido: List<LineaPedido>,
    instrumentos: List<Instrumento>,
    onDisminuir: (String) -> Unit,
    onEliminar: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi pedido") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (pedido.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No hay productos en tu pedido.")
                    Text("Total: $0.00")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = pedido,
                        key = { it.instrumentoId }
                    ) { linea ->
                        val instrumento = instrumentos.find { it.id == linea.instrumentoId }

                        if (instrumento != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(instrumento.nombre)
                                    Text("$${instrumento.precio} por unidad")
                                    Text("Cantidad: ${linea.cantidad}")
                                    Text(
                                        "Subtotal: $${
                                            calcularSubtotal(linea, instrumentos)
                                        }"
                                    )
                                }

                                Row {
                                    Button(onClick = { onDisminuir(linea.instrumentoId) }) {
                                        Text("-")
                                    }
                                    Button(onClick = { onEliminar(linea.instrumentoId) }) {
                                        Text("Eliminar")
                                    }
                                }
                            }

                            HorizontalDivider()
                        }
                    }
                }

                Text(
                    text = "Total: $${calcularTotal(pedido, instrumentos)}",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}