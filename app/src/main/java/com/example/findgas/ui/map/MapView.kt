package com.example.findgas.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.net.URLEncoder

// Map Function
@Composable
fun MapsView(navController: NavHostController, vm: MapsViewModel) {
    // Declaring variables
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val gasStations by vm.stations.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
    }
    val myLocationEnabled = remember { mutableStateOf(false) }
    // Map properties (Settings for the map)
    val properties = MapProperties(
        mapType = MapType.NORMAL,
        isMyLocationEnabled = myLocationEnabled.value
    )

    val routePoints by vm.routePoints.collectAsState()
    val destinationAddress by vm.destinationAddress.collectAsState()
    val waypoints by vm.waypoints.collectAsState()
    val currentLocation by vm.currentLocation.collectAsState()
    val gasStationStops by vm.gasStationStops.collectAsState()
    var initialLoad by remember { mutableStateOf(true) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    vm.setCurrentLocation(latLng)
                    if (initialLoad) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)
                        vm.fetchStations(lat = location.latitude, lng = location.longitude, MapFilter.GasStations)
                        initialLoad = false
                    }
                }
            }
        }
    }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            myLocationEnabled.value = true
        } else {
            myLocationEnabled.value = false
        }
    }


    // Ask permission automatically once when the composable loads
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(myLocationEnabled.value) {
        if (myLocationEnabled.value) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000
            ).build()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Composable for Google maps API
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            gasStations.forEach { station ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            station.geometry.location.lat,
                            station.geometry.location.lng
                        )
                    ),
                    title = station.name
                )
            }

            gasStationStops.forEach { stop ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            stop.geometry.location.lat,
                            stop.geometry.location.lng
                        )
                    ),
                    title = stop.name,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 8f
                )
            }
        }

        if (routePoints.isNotEmpty() && destinationAddress != null) {
            Column {
                Button(
                    onClick = {
                        val origin = currentLocation
                        val destination = destinationAddress
                        if (origin != null && destination != null) {
                            val waypointsString = waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }

                            val encodedDestination = URLEncoder.encode(destination, "UTF-8")

                            // The waypoints string of lat,lng|lat,lng doesn't need encoding. The pipes are separators.
                            val url = "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=$encodedDestination&waypoints=$waypointsString"

                            val gmmIntentUri = Uri.parse(url)
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Start Navigation")
                }
                Button(
                    onClick = { vm.setRoute(emptyList()) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear")
                }
            }
        }
    }

}
