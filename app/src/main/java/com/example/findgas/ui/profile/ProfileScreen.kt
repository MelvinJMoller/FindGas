package com.example.findgas.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.findgas.ui.theme.FindGasTheme

val isLoggedIn = false

@Composable
fun ProfileView(navController: NavHostController, content: @Composable () -> Unit = {}) {
    if (isLoggedIn == true) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            // Button 1
            Button(onClick =  {},
                modifier = Modifier.width(180.dp).height(100.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf5aa42)))
            { Text("Profile") }

            // Button 2
            Button(onClick =  {},
                modifier = Modifier.width(180.dp).height(100.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf5aa42)))
            { Text("Car") }

            // Button 3
            Button(onClick =  {},
                modifier = Modifier.width(180.dp).height(100.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf5aa42)))
            { Text("Settings") }
        }
    } else {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            // Log in Button
            Button(onClick =  {},
                modifier = Modifier.width(180.dp).height(100.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf5aa42)))
            { Text("Log In") }

            // Register Button
            Button(onClick =  {},
                modifier = Modifier.width(180.dp).height(100.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf5aa42)))
            { Text("Register") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FindGasTheme {
        //ProfileView()
        //FindGasBar()
    }
}