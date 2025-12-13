package com.example.findgas

import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {
    @GET("directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String
    ): DirectionsResponse
}