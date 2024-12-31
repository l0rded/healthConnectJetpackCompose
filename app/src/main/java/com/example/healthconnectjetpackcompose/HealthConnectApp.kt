package com.example.healthconnectjetpackcompose

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.healthconnectjetpackcompose.data.managers.HealthConnectManager
import com.example.healthconnectjetpackcompose.navigation.AppNavigation
import com.example.healthconnectjetpackcompose.ui.theme.HealthConnectJetpackComposeTheme


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HealthConnectApp(healthConnectManager: HealthConnectManager) {
    HealthConnectJetpackComposeTheme {
        val navController = rememberNavController()

        val scaffoldState = rememberScaffoldState()
        Scaffold(
            scaffoldState = scaffoldState,
        ) {
            AppNavigation(
                healthConnectManager = healthConnectManager,
                navController = navController,
            )
        }
    }
}
