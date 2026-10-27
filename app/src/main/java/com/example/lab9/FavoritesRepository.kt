package com.example.lab9

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<String>>

    suspend fun setFavorite(
        productId: String,
        isFavorite: Boolean
    ): Result<Unit>
}