package com.example.findgas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.Polyline

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
    val pointZero = (LatLng(0.0, 0.0))
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

        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = Color.Blue,
                width = 8f
            )
        }
    }
}

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dLat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dLng

        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }

    return poly
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindGasTheme {
        //MapsView(navController)
        //FindGasBar()
    }
}