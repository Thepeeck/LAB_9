package com.example.lab9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lab9.ui.theme.LAB9Theme

class MainActivity : ComponentActivity() {

    private val storeViewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            LAB9Theme {
                TiendaApp(viewModel = storeViewModel)
            }
        }
    }
}

@Composable
fun TiendaApp(
    viewModel: StoreViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val checkoutUiState by viewModel.checkoutUiState.collectAsStateWithLifecycle()
    val lastReceipt by viewModel.lastReceipt.collectAsStateWithLifecycle()

    TiendaNavigation(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onFavoritoToggle = viewModel::alternarFavorito,
        onDisminuirProducto = viewModel::disminuirProducto,
        onEliminarProducto = viewModel::eliminarProducto,
        onAgregarProducto = viewModel::agregarProducto,
        checkoutUiState = checkoutUiState,
        onFullNameChange = viewModel::onFullNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onBillingTypeChange = viewModel::onBillingTypeChange,
        onNitChange = viewModel::onNitChange,
        onBusinessNameChange = viewModel::onBusinessNameChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onConfirmOrder = viewModel::onConfirmOrder,
        onCatalogOrderChange = viewModel::onCatalogOrderChange,
                lastReceipt = lastReceipt

    )
}