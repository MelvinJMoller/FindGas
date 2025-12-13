package com.example.findgas

data class PlacesResponse(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val geometry: Geometry
)

data class Geometry(
    val location: LocationLatLng
)

data class LocationLatLng(
    val lat: Double,
    val lng: Double
)

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)