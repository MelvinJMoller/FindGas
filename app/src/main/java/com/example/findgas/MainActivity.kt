package com.example.findgas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.findgas.ui.theme.FindGasTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Polyline
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindGasTheme {
                val navController = rememberNavController()
                val vm: MapsViewModel = viewModel()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { FindGasBar(navController, vm) })
                { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        Column(Modifier.fillMaxSize()) {
                            Navigation(navController, vm)
                        }
                    }
                }
            }
        }
    }
}

// Map Function
@Composable
fun MapsView(navController: NavHostController, vm: MapsViewModel) {
    // Declaring variables
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val trelleborg = LatLng(55.3722, 13.1811)
    val gasStations by vm.stations.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trelleborg, 17f)
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

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            myLocationEnabled.value = true
            // Move camera to current location
            // Check permission safely before accessing location
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        vm.setCurrentLocation(latLng)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)

                        vm.fetchStations(
                            lat = location.latitude,
                            lng = location.longitude,
                            MapFilter.GasStations
                        )
                    }
                }
            }

        } else {
            myLocationEnabled.value = false
        }
    }


    // Ask permission automatically once when the composable loads
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindGasTheme {
        //MapsView(navController)
        //FindGasBar()
    }
}
