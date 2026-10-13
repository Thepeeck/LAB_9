package com.example.lab9

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object OrderApiClient {

    private const val BASE_URL = "https://6acd779ab406cb8ec0c71c1a.mockapi.io/api/v1/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val api: OrderApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OrderApi::class.java)
    }
}