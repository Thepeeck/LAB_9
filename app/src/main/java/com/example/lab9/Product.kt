package com.example.lab9

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val brandId: String,
    val stock: Int,
    val imageUrl: String
)