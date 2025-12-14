package com.example.findgas.data

import com.google.gson.annotations.SerializedName

// For Places API
data class PlacesResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    val name: String,
    val vicinity: String,
    val geometry: Geometry
)

// --- NEW: Specific data classes for Geocoding API ---
data class GeocodeApiResponse(
    val results: List<GeocodeApiResult>,
    val status: String
)

data class GeocodeApiResult(
    val geometry: GeocodeApiGeometry
)

data class GeocodeApiGeometry(
    val location: GeocodeApiLocation
)

data class GeocodeApiLocation(
    val lat: Double,
    val lng: Double
)
// --- End of Geocoding classes ---


// Common data classes used by Places and Directions
data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

// For Directions API
data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    @SerializedName("overview_polyline") val overview_polyline: Polyline,
    val legs: List<Leg>
)

data class Polyline(
    val points: String
)

data class Leg(
    val distance: Distance
)

data class Distance(
    val text: String,
    val value: Int // Distance in meters
)
