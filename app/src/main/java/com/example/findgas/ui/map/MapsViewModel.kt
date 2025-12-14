package com.example.findgas.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findgas.BuildConfig
import com.example.findgas.data.PlaceResult
import com.example.findgas.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteDetails(val points: List<LatLng>, val distance: Double) // Distance in KM

class MapsViewModel : ViewModel() {

    private val _stations = MutableStateFlow<List<PlaceResult>>(emptyList())
    val stations = _stations

    private val _filter = MutableStateFlow<List<PlaceResult>>(emptyList())
    val filter = _filter

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints = _routePoints

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation

    private val _destinationAddress = MutableStateFlow<String?>(null)
    val destinationAddress = _destinationAddress

    private val _waypoints = MutableStateFlow<List<LatLng>>(emptyList())
    val waypoints = _waypoints

    private val _gasStationStops = MutableStateFlow<List<PlaceResult>>(emptyList())
    val gasStationStops = _gasStationStops

    fun setDestinationAddress(address: String?) {
        _destinationAddress.value = address
    }

    fun setCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun setRoute(points: List<LatLng>) {
        _routePoints.value = points
        if (points.isEmpty()) {
            _waypoints.value = emptyList()
            _destinationAddress.value = null
            _gasStationStops.value = emptyList()
        }
    }

    suspend fun getRoute(origin: LatLng, dest: LatLng, waypoints: List<LatLng> = emptyList()): RouteDetails? {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY
        val waypointsString = waypoints.joinToString(separator = "|") { "${it.latitude},${it.longitude}" }

        return try {
            val response = RetrofitClient.directionsApi.getRoute(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${dest.latitude},${dest.longitude}",
                waypoints = waypointsString,
                mode = "driving",
                apiKey = key,
            )
            Log.d("ROUTE", "Status: ${response.status}")

            if (response.routes.isNotEmpty()) {
                val route = response.routes.first()
                val leg = route.legs.first()
                val distance = leg.distance.value / 1000.0 // Convert meters to KM
                val points = decodePolyline(route.overview_polyline.points)
                RouteDetails(points, distance)
            } else {
                null
            }

        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun planTrip(origin: LatLng, destination: LatLng, currentFuel: Double, maxFuel: Double, consumption: Double): Boolean {
        Log.d("TripPlanner", "Starting trip planning...")
        Log.d("TripPlanner", "Origin: $origin, Destination: $destination")
        Log.d("TripPlanner", "Fuel -> Current: $currentFuel L, Max: $maxFuel L, Consumption: $consumption L/10km")

        val initialCarRange = (currentFuel / consumption) * 10
        val maxCarRange = (maxFuel / consumption) * 10
        val safetyBuffer = 50.0 // 50km buffer

        Log.d("TripPlanner", "Initial Range: $initialCarRange km, Max Range: $maxCarRange km")

        val finalWaypoints = mutableListOf<LatLng>()
        val foundStops = mutableListOf<PlaceResult>()
        var legOrigin = origin
        var currentFuelRange = initialCarRange

        for (i in 0..15) { // Hard limit to 15 stops
            Log.d("TripPlanner", "--- Planning Leg ${i + 1} ---")
            Log.d("TripPlanner", "Leg Origin: $legOrigin, Current Fuel Range: $currentFuelRange km")

            val routeDetails = getRoute(legOrigin, destination)
            if (routeDetails == null) {
                Log.e("TripPlanner", "Could not find route for this leg. Aborting.")
                setRoute(emptyList())
                return false
            }

            Log.d("TripPlanner", "Distance for current leg: ${routeDetails.distance} km")

            if (currentFuelRange >= routeDetails.distance) {
                Log.d("TripPlanner", "Destination is reachable on this tank. Finishing plan.")
                break
            }

            Log.d("TripPlanner", "Destination not reachable. Searching for a gas stop.")
            var distanceTraveledOnSegment = 0.0
            var searchPoint: LatLng? = null

            for (j in 0 until routeDetails.points.size - 1) {
                val pointStart = routeDetails.points[j]
                val pointEnd = routeDetails.points[j + 1]
                val legDistance = calculateDistance(pointStart, pointEnd) // Using Haversine for point-to-point approximation

                if (distanceTraveledOnSegment + legDistance > currentFuelRange - safetyBuffer) {
                    searchPoint = pointEnd
                    Log.d("TripPlanner", "Fuel will be low near $searchPoint. Searching for stations.")
                    break
                }
                distanceTraveledOnSegment += legDistance
            }

            if (searchPoint == null) {
                Log.e("TripPlanner", "Could not find a point to search for stations. Fallback exit.")
                break // Fallback to prevent infinite loop
            }

            val gasStations = findNearbyGasStations(searchPoint)
            if (gasStations.isEmpty()) {
                Log.e("TripPlanner", "No gas stations found near $searchPoint. Aborting.")
                break
            }

            val chosenGasStation = gasStations.first()
            val gasStationLatLng = LatLng(chosenGasStation.geometry.location.lat, chosenGasStation.geometry.location.lng)
            Log.d("TripPlanner", "Found best gas station: ${chosenGasStation.name} at $gasStationLatLng")

            finalWaypoints.add(gasStationLatLng)
            foundStops.add(chosenGasStation)

            // Set up for the next leg
            legOrigin = gasStationLatLng
            currentFuelRange = maxCarRange // Refuel!
        }

        Log.d("TripPlanner", "Planning complete. Found ${foundStops.size} stops.")
        _waypoints.value = finalWaypoints
        _gasStationStops.value = foundStops
        val finalRoute = getRoute(origin, destination, finalWaypoints)
        if (finalRoute != null) {
            setRoute(finalRoute.points)
            return true
        }
        setRoute(emptyList())
        return false
    }

    private suspend fun findNearbyGasStations(location: LatLng): List<PlaceResult> {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY
        return try {
            val response = RetrofitClient.api.getNearbyGasStations(
                location = "${location.latitude},${location.longitude}",
                apiKey = key
            )
            response.results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371 // Radius of the earth in km
        val lat1 = p1.latitude
        val lon1 = p1.longitude
        val lat2 = p2.latitude
        val lon2 = p2.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    suspend fun geocodeAddress(address: String): LatLng? {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY
        return try {
            val response = RetrofitClient.directionsApi.geocode(
                address = address,
                apiKey = key
            )
            if (response.status == "OK" && response.results.isNotEmpty()) {
                val location = response.results.first().geometry.location
                LatLng(location.lat, location.lng)
            } else {
                Log.e("Geocoding", "Geocoding failed with status: ${response.status}")
                null
            }
        } catch (e: Exception) { 
            Log.e("Geocoding", "Geocoding exception", e)
            e.printStackTrace()
            null
        }
    }

    fun fetchStations(lat: Double, lng: Double, mapFilter: MapFilter) {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY

        viewModelScope.launch {
            try {
                val response = when(mapFilter) {
                    MapFilter.GasStations -> RetrofitClient.api.getNearbyGasStations(
                        location = "$lat,$lng",
                        apiKey = key
                    )
                    MapFilter.Electric -> RetrofitClient.api.getNearbyElectricChargers(
                        location = "$lat,$lng",
                        apiKey = key
                    )
                    MapFilter.Workshops -> RetrofitClient.api.getNearbyWorkshops(
                        location = "$lat,$lng",
                        apiKey = key
                    )
                    MapFilter.Rentals -> RetrofitClient.api.getNearbyRentals(
                        location = "$lat,$lng",
                        apiKey = key
                    )
                    MapFilter.Washes -> RetrofitClient.api.getNearbyCarWashes(
                        location = "$lat,$lng",
                        apiKey = key
                    )
                }

                _stations.value = response.results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun decodePolyline(encodedPath: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encodedPath.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}

enum class MapFilter(name: String) {
    GasStations("Gas Stations"),
    Electric("Electric Car"),
    Workshops("Workshop"),
    Rentals("Car Rentals"),
    Washes("Car Wash")
}
