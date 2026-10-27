package com.example.lab9

import kotlinx.coroutines.flow.Flow

interface CatalogPreferencesRepository {
    fun observeCatalogOrder(): Flow<String>

    suspend fun setCatalogOrder(
        order: String
    ): Result<Unit>
}