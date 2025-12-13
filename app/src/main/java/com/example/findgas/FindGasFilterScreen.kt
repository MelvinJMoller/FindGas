package com.example.findgas

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findgas.ui.theme.FindGasTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

val itemChecked = null
data class DropDownItem(val text: String)

@Composable
fun DropdownMenuWithDetails(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    vm: MapsViewModel,
    anchor: @Composable () -> Unit,
) {
    val activity = LocalActivity.current
    var selectedItem by remember { mutableStateOf<MapFilter?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val items = listOf(MapFilter.GasStations, MapFilter.Electric, MapFilter.Workshops, MapFilter.Washes, MapFilter.Rentals,)
    val myLocationEnabled = remember { mutableStateOf(false) }
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

                        vm.fetchStations(
                            lat = location.latitude,
                            lng = location.longitude,
                            selectedItem?:MapFilter.GasStations
                        )
                    }
                }
            }

        } else {
            myLocationEnabled.value = false
        }
    }


    Box {
        anchor()

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            containerColor = Color(0x908f3306)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if(item != selectedItem) {
                                Text(
                                    text = item.name,
                                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                )
                            }

                            if(item == selectedItem) {
                                Text(
                                    text = item.name,
                                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                )
                            }

                            // Icons when Clicked on Filters
//                            if (item == selectedItem) {
//                                Icon(
//                                    imageVector = Icons.Default.CheckCircle,
//                                    contentDescription = "Selected",
//                                    modifier = Modifier.size(22.dp),
//                                    //tint = Color.Green
//                                )
//                            }
                        }
                    },
                    onClick = {
                        selectedItem = item
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )
            }
        }
    }
}



@Composable
fun FindGasFilterScreen(content: @Composable () -> Unit = {}) {
    Box {

    }
}

@Preview(showBackground = true)
@Composable
fun FindGasFilterScreenPreview() {
    FindGasTheme {
        FindGasFilterScreen()
    }
}