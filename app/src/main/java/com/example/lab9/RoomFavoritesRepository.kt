package com.example.lab9

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(
    private val dao: StoreDao
) : FavoritesRepository {

    override fun observeFavoriteIds(): Flow<Set<String>> {
        return dao.observeFavorites().map { entities ->
            entities.map { entity ->
                entity.instrumentoId
            }.toSet()
        }
    }

    override suspend fun setFavorite(
        productId: String,
        isFavorite: Boolean
    ): Result<Unit> {
        return captureRepositoryResult {
            if (isFavorite) {
                dao.insertFavorite(
                    FavoriteEntity(
                        instrumentoId = productId
                    )
                )
            } else {
                dao.deleteFavorite(productId)
            }
        }
    }
}