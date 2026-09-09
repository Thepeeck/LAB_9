package com.example.lab9

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StoreUiState(
    val instrumentos: List<Instrumento>,
    val marcas: List<Marca>,
    val favoritos: Set<String> = emptySet()
)

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            instrumentos = listOf(
                Instrumento(
                    id = "1",
                    nombre = "Guitarra",
                    descripcion = "Guitarra acústica",
                    precio = 250.0,
                    marcaId = "m1"
                ),
                Instrumento(
                    id = "2",
                    nombre = "Piano",
                    descripcion = "Piano digital",
                    precio = 800.0,
                    marcaId = "m2"
                ),
                Instrumento(
                    id = "3",
                    nombre = "Batería",
                    descripcion = "Batería acústica",
                    precio = 600.0,
                    marcaId = "m1"
                )
            ),
            marcas = listOf(
                Marca(
                    id = "m1",
                    nombre = "Yamaha",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion = "Fabricante de instrumentos musicales."
                ),
                Marca(
                    id = "m2",
                    nombre = "Casio",
                    rol = "Fabricante",
                    ubicacion = "Japón",
                    descripcion = "Fabricante de instrumentos electrónicos."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun alternarFavorito(instrumentoId: String) {
        val favoritosActuales = _uiState.value.favoritos

        val nuevosFavoritos =
            if (instrumentoId in favoritosActuales) {
                favoritosActuales - instrumentoId
            } else {
                favoritosActuales + instrumentoId
            }

        android.util.Log.d("FavoritoDebug", "id=$instrumentoId antes=$favoritosActuales despues=$nuevosFavoritos")

        _uiState.value = _uiState.value.copy(
            favoritos = nuevosFavoritos
        )
    }
}