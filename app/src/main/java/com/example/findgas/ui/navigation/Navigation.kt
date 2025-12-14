package com.example.findgas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.findgas.ui.map.MapsView
import com.example.findgas.ui.map.MapsViewModel
import com.example.findgas.ui.profile.ProfileView
import com.example.findgas.ui.TripPlannerView

sealed class Routes(val route: String) {
    object HomeScreen : Routes("HomeScreen")
    object Profile : Routes("Profile")
    object TripPlanner: Routes("TripPlanner")
}

@Composable
fun Navigation(navController: NavHostController, vm: MapsViewModel) {
    NavHost(navController = navController, startDestination = Routes.HomeScreen.route) {

        composable(Routes.HomeScreen.route) {
            MapsView(navController, vm)
        }
        composable(Routes.Profile.route) {
            ProfileView(navController)
        }
        composable(Routes.TripPlanner.route) {
            TripPlannerView(navController, vm)
        }
    }
}