
package com.example.lab9

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey
    val instrumentoId: String,
    val cantidad: Int
)
