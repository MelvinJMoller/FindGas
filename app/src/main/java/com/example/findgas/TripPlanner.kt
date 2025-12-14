package com.example.findgas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.findgas.ui.theme.FindGasTheme
import kotlinx.coroutines.launch

// Function for the Trip Planner
@Composable
fun TripPlannerView(navController: NavController, mapsViewModel: MapsViewModel) {

    // Declaring Variables
    var destinationVal by remember { mutableStateOf("") }
    var currentFuel by remember { mutableStateOf("") }
    var maxFuel by remember { mutableStateOf("") }
    var consumption by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentLocation by mapsViewModel.currentLocation.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Roadtrip Planner",
            fontSize = 24.sp
        )

        // Input for destination
        TextField(
            value = destinationVal,
            onValueChange = {destinationVal = it},
            label = {Text("Enter your destination:")},
            modifier = Modifier.fillMaxWidth()
        )
        // Input for how much Fuel currently in the car
        TextField(
            value = currentFuel,
            onValueChange = {currentFuel = it},
            label = {Text("How much fuel do you have (in Liters):")},
            modifier = Modifier.fillMaxWidth()
        )
        // Input for how much the maximum amount of fuel is in the car
        TextField(
            value = maxFuel,
            onValueChange = {maxFuel = it},
            label = {Text("What is your maximum fuel (in Liters):")},
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = consumption,
            onValueChange = {consumption = it},
            label = {Text("Litre per 10km:")},
            modifier = Modifier.fillMaxWidth()
        )
        // Confirmation button
        Button(onClick = {
            val currentFuelVal = currentFuel.toDoubleOrNull()
            val maxFuelVal = maxFuel.toDoubleOrNull()
            val consumptionVal = consumption.toDoubleOrNull()

            if (destinationVal.isBlank()) {
                Toast.makeText(context, "Please enter a destination.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (currentFuelVal == null || maxFuelVal == null || consumptionVal == null) {
                Toast.makeText(context, "Please enter valid numbers for fuel and consumption.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (consumptionVal == 0.0) {
                Toast.makeText(context, "Consumption cannot be zero.", Toast.LENGTH_SHORT).show()
                return@Button
            }

            scope.launch {
                if (currentLocation == null) {
                    Toast.makeText(context, "Could not get current location.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val destination = mapsViewModel.geocodeAddress(destinationVal)
                if (destination == null) {
                    Toast.makeText(context, "Could not find destination address.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                mapsViewModel.setDestinationAddress(destinationVal)
                val success = mapsViewModel.planTrip(
                    currentLocation!!,
                    destination,
                    currentFuelVal,
                    maxFuelVal,
                    consumptionVal
                )

                if (success) {
                    navController.navigate(Routes.HomeScreen.route)
                } else {
                    Toast.makeText(context, "No route could be planned for the trip.", Toast.LENGTH_SHORT).show()
                    mapsViewModel.setDestinationAddress(null)
                }
            }

        },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
        ) {
            Text("Plan")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun TripPlannerPreview() {
    FindGasTheme {
        //TripPlannerView()
        //FindGasBar()
    }
}