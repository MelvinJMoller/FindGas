package com.example.findgas.network

import com.example.findgas.data.DirectionsResponse
import com.example.findgas.data.GeocodeApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {
    @GET("directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String,
    ): DirectionsResponse

    @GET("geocode/json")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodeApiResponse // Use the new, specific GeocodeApiResponse class
}
