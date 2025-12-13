package com.example.findgas

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    // Searches for nearby Gas Stations, This is Default
    @GET("nearbysearch/json")
    suspend fun getNearbyGasStations(
        @Query("location") location: String,  // "lat,lng"
        @Query("radius") radius: Int = 3000, // in meters
        @Query("type") type: String = "gas_station",
        @Query("key") apiKey: String
    ): PlacesResponse

    // Searches for nearby Gas Stations with support for electric car charging
    @GET("nearbysearch/json")
    suspend fun getNearbyElectricChargers(
        @Query("location") location: String,  // "lat,lng"
        @Query("radius") radius: Int = 3000, // in meters
        @Query("type") type: String = "electric_vehicle_charging_station",
        @Query("key") apiKey: String
    ): PlacesResponse

    // Searches for Workshops
    @GET("nearbysearch/json")
    suspend fun getNearbyWorkshops(
        @Query("location") location: String,  // "lat,lng"
        @Query("radius") radius: Int = 3000, // in meters
        @Query("type") type: String = "car_repair",
        @Query("key") apiKey: String
    ): PlacesResponse

    // Searches for Car Rentals
    @GET("nearbysearch/json")
    suspend fun getNearbyRentals(
        @Query("location") location: String,  // "lat,lng"
        @Query("radius") radius: Int = 3000, // in meters
        @Query("type") type: String = "car_rental",
        @Query("key") apiKey: String
    ): PlacesResponse

    // Searches for Car Washes
    @GET("nearbysearch/json")
    suspend fun getNearbyCarWashes(
        @Query("location") location: String,  // "lat,lng"
        @Query("radius") radius: Int = 3000, // in meters
        @Query("type") type: String = "car_wash",
        @Query("key") apiKey: String
    ): PlacesResponse
}

// Other Types: car_repair, car_dealer, car_rental, car_wash, electric_vehicle_charging_station