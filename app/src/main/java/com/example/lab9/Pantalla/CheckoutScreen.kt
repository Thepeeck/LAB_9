package com.example.lab9.Pantalla

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lab9.BillingType
import com.example.lab9.CheckoutUiState
import com.example.lab9.Instrumento
import com.example.lab9.LineaPedido
import com.example.lab9.PaymentMethod
import com.example.lab9.calcularSubtotal
import com.example.lab9.calcularTotal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    pedido: List<LineaPedido>,
    instrumentos: List<Instrumento>,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController =
        LocalSoftwareKeyboardController.current

    val nitFocusRequester = remember {
        FocusRequester()
    }

    val orderUnits = pedido.sumOf { line ->
        line.cantidad
    }

    val total = calcularTotal(
        pedido = pedido,
        instrumentos = instrumentos
    )

    val formattedTotal = String.format(
        Locale.US,
        "%.2f",
        total
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Checkout")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Total: $$formattedTotal",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resumen del pedido",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$orderUnits unidades"
            )

            pedido.forEach { line ->
                val instrumento = instrumentos.find { product ->
                    product.id == line.instrumentoId
                }

                if (instrumento != null) {
                    val subtotal = calcularSubtotal(
                        linea = line,
                        instrumentos = instrumentos
                    )

                    val formattedSubtotal = String.format(
                        Locale.US,
                        "%.2f",
                        subtotal
                    )

                    Text(
                        text = "${instrumento.nombre} " +
                            "(x${line.cantidad}) · " +
                            "Subtotal: $$formattedSubtotal",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre completo *")
                },
                placeholder = {
                    Text("Ej. María Morales")
                },
                singleLine = true,
                isError = uiState.fullNameError != null,
                supportingText = {
                    uiState.fullNameError?.let { error ->
                        Text(error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(
                            FocusDirection.Next
                        )
                    }
                )
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = {
                    Text("Teléfono / WhatsApp *")
                },
                placeholder = {
                    Text("Ej. 55123456")
                },
                singleLine = true,
                isError = uiState.phoneError != null,
                supportingText = {
                    uiState.phoneError?.let { error ->
                        Text(error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction =
                        if (
                            uiState.billingType ==
                            BillingType.NIT
                        ) {
                            ImeAction.Next
                        } else {
                            ImeAction.Done
                        }
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (
                            uiState.billingType ==
                            BillingType.NIT
                        ) {
                            nitFocusRequester.requestFocus()
                        }
                    },
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Facturación *",
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.selectableGroup()
            ) {
                listOf(
                    BillingType.CF,
                    BillingType.NIT
                ).forEach { type ->
                    val label =
                        if (type == BillingType.CF) {
                            "Consumidor Final (CF)"
                        } else {
                            "Factura con NIT"
                        }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected =
                                    uiState.billingType == type,
                                role = Role.RadioButton,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onBillingTypeChange(type)
                                }
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected =
                                uiState.billingType == type,
                            onClick = null
                        )

                        Text(label)
                    }
                }
            }

            ConditionalBillingFields(
                uiState = uiState,
                nitFocusRequester = nitFocusRequester,
                onNitChange = onNitChange,
                onBusinessNameChange =
                    onBusinessNameChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Método de pago *",
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.selectableGroup()
            ) {
                listOf(
                    PaymentMethod.CASH_ON_DELIVERY,
                    PaymentMethod.BANK_TRANSFER
                ).forEach { method ->
                    val label =
                        if (
                            method ==
                            PaymentMethod.CASH_ON_DELIVERY
                        ) {
                            "Efectivo contra entrega"
                        } else {
                            "Transferencia bancaria"
                        }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected =
                                    uiState.paymentMethod ==
                                        method,
                                role = Role.RadioButton,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onPaymentMethodChange(
                                        method
                                    )
                                }
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected =
                                uiState.paymentMethod ==
                                    method,
                            onClick = null
                        )

                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ReactiveConfirmButton(
                uiState = uiState,
                orderUnits = orderUnits,
                onConfirmOrder = onConfirmOrder
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
