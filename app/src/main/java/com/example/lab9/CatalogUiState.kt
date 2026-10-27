package com.example.lab9

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data object Success : CatalogUiState
    data class Error(
        val message: String
    ) : CatalogUiState
}