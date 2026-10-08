
package com.example.lab9

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE instrumentoId = :id")
    suspend fun deleteFavorite(id: String)

    @Query("SELECT * FROM order_lines")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderLine(line: OrderLineEntity)

    @Query("DELETE FROM order_lines WHERE instrumentoId = :id")
    suspend fun deleteOrderLine(id: String)

    @Query("DELETE FROM order_lines")
    suspend fun clearOrderLines()
}
