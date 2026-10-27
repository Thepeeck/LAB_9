package com.example.lab9

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreCatalogPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : CatalogPreferencesRepository {

    override fun observeCatalogOrder(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[CATALOG_ORDER_KEY] ?: "name"
            }
    }

    override suspend fun setCatalogOrder(
        order: String
    ): Result<Unit> {
        return captureRepositoryResult {
            require(order == "name" || order == "price") {
                "Unsupported catalog order."
            }

            dataStore.edit { preferences ->
                preferences[CATALOG_ORDER_KEY] = order
            }

            Unit
        }
    }
}