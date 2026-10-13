package com.example.lab9

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body order: CreateOrderDto): OrderDto

    @GET("orders")
    suspend fun getOrders(
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("order") order: String = "desc"
    ): List<OrderDto>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDto
}