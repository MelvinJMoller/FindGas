package com.example.findgas.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.findgas.data.PlaceResult
import com.example.findgas.ui.filter.DropdownMenuWithDetails
import com.example.findgas.ui.map.MapsViewModel
import com.example.findgas.ui.navigation.Routes
import com.example.findgas.ui.theme.FindGasTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FindGasBar(navController: NavController, vm: MapsViewModel, content: @Composable () -> Unit = {}) {
    Column {

        val gasStations by vm.stations.collectAsState()

        val context = LocalContext.current

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


        content()
        Row {

            // DropdownMenu Variables
            var dropdownExpanded by remember { mutableStateOf(false) }

            // DropdownMenu for the Filter Button
            DropdownMenuWithDetails(expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                vm = vm,
                anchor = {
                    // FILTER Button //
                    Button(onClick = {
                        dropdownExpanded = !dropdownExpanded
                    },
                        modifier = Modifier.width(120.dp).height(100.dp),
                        // shape = RoundedCornerShape(topStart = 50.dp),
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, Color.Black),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8f3306)
                        ))
                    //{ Text("Filter") }
                    {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = null, modifier = Modifier.size(50.dp))
                    }
                })


            // SEARCH Button //
            Button(onClick = {

                // Ensure permission granted
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) return@Button

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {

                        val userPos = LatLng(location.latitude, location.longitude)

                        // 1. nearest of the ACTIVE FILTER (vm.stations)
                        val nearest = findNearestFilteredStation(userPos, gasStations)

                        if (nearest != null) {

                            val dest = LatLng(
                                nearest.geometry.location.lat,
                                nearest.geometry.location.lng
                            )

                            // 2. fetch route -> ViewModel function you already have
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = vm.getRoute(userPos, dest)

                                withContext(Dispatchers.Main) {
                                    vm.setRoute(result?.points ?: emptyList())
                                }
                            }
                        }
                    }
                }

            },
                modifier = Modifier.width(180.dp).height(100.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8f3306)
                ))
            //{ Text("Search") }
            { Icon(imageVector = Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(50.dp))}

            // ROADPLANNER Button //
            // This was originally the profile button
            Button(onClick = {
                navController.navigate(route = Routes.TripPlanner.route)
            },
                modifier = Modifier.width(120.dp).height(100.dp),
                shape = RoundedCornerShape(topEnd = 70.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8f3306)
                ))
            //{ Text("Profile") }
            { Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(50.dp)) }
        }
    }
}

fun findNearestFilteredStation(
    user: LatLng,
    stations: List<PlaceResult>
): PlaceResult? {
    return stations.minByOrNull { station ->
        val dx = station.geometry.location.lat - user.latitude
        val dy = station.geometry.location.lng - user.longitude
        dx * dx + dy * dy
    }
}

@Preview(showBackground = true)
@Composable
fun FindGasBarPreview() {
    FindGasTheme {
        //FindGasBar()
    }
}
