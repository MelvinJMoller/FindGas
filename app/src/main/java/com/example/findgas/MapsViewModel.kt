package com.example.findgas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _stations = MutableStateFlow<List<PlaceResult>>(emptyList())
    val stations = _stations

    private val _filter = MutableStateFlow<List<PlaceResult>>(emptyList())
    val filter = _filter

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints = _routePoints

    fun setRoute(points: List<LatLng>) {
        _routePoints.value = points
    }

    suspend fun getRoute(origin: LatLng, dest: LatLng): List<LatLng> {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY

        return try {
            val response = RetrofitClient.directionsApi.getRoute(

                origin = "${origin.latitude},${origin.longitude}",
                destination = "${dest.latitude},${dest.longitude}",
                mode = "driving",
                apiKey = key,
            )
            Log.d("ROUTE", "Status: ${response.status}")
            Log.d("ROUTE", "Routes count: ${response.routes.size}")

            val encoded = response.routes.firstOrNull()
                ?.overview_polyline?.points ?: ""

            decodePolyline(encoded)

        }
        catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
}

enum class MapFilter(name: String) {
    GasStations("Gas Stations"),
    Electric("Electric Car"),
    Workshops("Workshop"),
    Rentals("Car Rentals"),
    Washes("Car Wash")
}