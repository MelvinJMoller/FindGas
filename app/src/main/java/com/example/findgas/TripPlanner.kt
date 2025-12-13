package com.example.findgas

import android.webkit.WebSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.findgas.ui.theme.FindGasTheme
import org.w3c.dom.Text

// Function for the Trip Planner
@Composable
fun TripPlannerView(navController: NavController) {
    // 3 Inputs??? "Destination", "How Much Fuel in the Car right now", "Maximum amount of Fuel in the tank"

    // Declaring Variables
    var destinationVal by remember { mutableStateOf("") }
    var currentFuel by remember { mutableStateOf("") }
    var maxFuel by remember { mutableStateOf("") }

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
            label = {Text("How much fuel do you have in your tank:")},
            modifier = Modifier.fillMaxWidth()
        )
        // Input for how much the maximum amount of fuel is in the car
        TextField(
            value = maxFuel,
            onValueChange = {maxFuel = it},
            label = {Text("What is your maximum amount of fuel:")},
            modifier = Modifier.fillMaxWidth()
        )
        // Confirmation button
        Button(onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
        ) {
            Text("Search")
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