
package com.example.lab9.Pantalla

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab9.OrderDetailUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    state: OrderDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del pedido") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                OrderDetailUiState.Idle,
                OrderDetailUiState.Loading -> {
                    CircularProgressIndicator()
                }

                OrderDetailUiState.NotFound -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pedido no encontrado")
                        Text("Este pedido ya no existe en el servidor.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onBack) {
                            Text("Volver a Mis pedidos")
                        }
                    }
                }

                is OrderDetailUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }

                is OrderDetailUiState.Success -> {
                    val order = state.order

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Pedido #${order.id}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text("Fecha: ${order.createdAt}")
                            HorizontalDivider()
                        }

                        items(order.lines) { line ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(line.name)
                                    Text("Cantidad: ${line.quantity}")
                                    Text(
                                        "Precio unitario: Q" +
                                            String.format(
                                                Locale.US,
                                                "%.2f",
                                                line.unitPrice
                                            )
                                    )
                                    Text(
                                        "Subtotal: Q" +
                                            String.format(
                                                Locale.US,
                                                "%.2f",
                                                line.unitPrice * line.quantity
                                            )
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider()
                            Text(
                                "Total: Q" + String.format(
                                    Locale.US,
                                    "%.2f",
                                    order.total
                                ),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
