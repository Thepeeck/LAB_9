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

    TiendaNavigation(
        uiState = uiState,
        onFavoritoToggle = viewModel::alternarFavorito
    )
}
