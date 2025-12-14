package com.example.findgas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.findgas.ui.FindGasBar
import com.example.findgas.ui.map.MapsViewModel
import com.example.findgas.ui.navigation.Navigation
import com.example.findgas.ui.theme.FindGasTheme

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
