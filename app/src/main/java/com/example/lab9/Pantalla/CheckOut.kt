package com.example.lab9.Pantalla

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lab9.BillingType
import com.example.lab9.CheckoutUiState

@Composable
fun ConditionalBillingFields(
    uiState: CheckoutUiState,
    nitFocusRequester: FocusRequester,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController =
        LocalSoftwareKeyboardController.current

    AnimatedVisibility(
        visible = uiState.billingType == BillingType.NIT,
        modifier = modifier
    ) {
        Column {
            Text(
                text = "DATOS DE FACTURACIÓN FISCAL",
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 8.dp
                )
            )

            OutlinedTextField(
                value = uiState.nit,
                onValueChange = onNitChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nitFocusRequester),
                label = {
                    Text("NIT *")
                },
                singleLine = true,
                isError = uiState.nitError != null,
                supportingText = {
                    uiState.nitError?.let { error ->
                        Text(error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
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
                value = uiState.businessName,
                onValueChange = onBusinessNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = {
                    Text("Razón Social / Nombre fiscal *")
                },
                singleLine = true,
                isError = uiState.businessNameError != null,
                supportingText = {
                    uiState.businessNameError?.let { error ->
                        Text(error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            )
        }
    }
}

@Composable
fun ReactiveConfirmButton(
    uiState: CheckoutUiState,
    orderUnits: Int,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUiState by rememberUpdatedState(uiState)
    val currentOrderUnits by rememberUpdatedState(orderUnits)

    val isConfirmEnabled by remember {
        derivedStateOf {
            currentUiState.isFormValid &&
                currentOrderUnits > 0
        }
    }

    Column(modifier = modifier) {
        Button(
            onClick = onConfirmOrder,
            enabled = isConfirmEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar pedido")
        }

        if (!isConfirmEnabled) {
            Text(
                text = if (orderUnits == 0) {
                    "Tu pedido no tiene productos."
                } else {
                    "Completa los campos obligatorios para continuar."
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
