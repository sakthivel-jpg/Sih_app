package com.example.agarbattidryer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.agarbattidryer.navigation.AppNavHost
import com.example.agarbattidryer.ui.theme.AgarbattiDryerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as AgarbattiDryerApplication).container
        val deviceRepository = appContainer.deviceRepository
        val historyRepository = appContainer.historyRepository
        setContent {
            AgarbattiDryerTheme {
                AppNavHost(
                    deviceRepository = deviceRepository,
                    historyRepository = historyRepository
                )
            }
        }
    }
}
