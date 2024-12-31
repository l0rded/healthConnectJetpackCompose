package com.example.healthconnectjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthconnectjetpackcompose.data.managers.HealthConnectManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthConnectApp(healthConnectManager = HealthConnectManager(this))
        }
    }
}