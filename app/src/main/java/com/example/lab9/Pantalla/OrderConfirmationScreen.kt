package com.example.lab9.Pantalla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab9.BillingType
import com.example.lab9.OrderReceipt
import com.example.lab9.PaymentMethod
import java.util.Locale

@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val billingLabel =
        if (receipt.billingType == BillingType.CF) {
            "CF (Consumidor Final)"
        } else {
            "Factura con NIT"
        }

    val paymentLabel =
        if (receipt.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            "Efectivo contra entrega"
        } else {
            "Transferencia bancaria"
        }

    val formattedTotal = String.format(
        Locale.US,
        "%.2f",
        receipt.total
    )

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Pedido confirmado!",
                fontWeight = FontWeight.Bold
            )

            Text(text = "Orden registrada exitosamente en su tienda.")

            Spacer(modifier = Modifier.height(24.dp))

            listOf(
                "Folio:" to receipt.folio,
                "Cliente:" to receipt.customerName,
                "Facturación:" to billingLabel,
                "Método de pago:" to paymentLabel
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label)
                    Text(text = value)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total del pedido:",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$$formattedTotal",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackToCatalog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al catálogo")
            }
        }
    }
}