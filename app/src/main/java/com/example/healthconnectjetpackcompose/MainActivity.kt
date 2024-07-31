package com.example.healthconnectjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthconnectjetpackcompose.model.HealthConnectManager
import com.example.healthconnectjetpackcompose.ui.theme.HealthConnectJetpackComposeTheme
import com.example.healthconnectjetpackcompose.view.screens.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthConnectApp(healthConnectManager = HealthConnectManager(this))
        }
    }
}