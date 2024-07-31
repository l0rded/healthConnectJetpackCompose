package com.example.healthconnectjetpackcompose

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthconnectjetpackcompose.model.HealthConnectManager
import com.example.healthconnectjetpackcompose.navigation.AppNavigation
import com.example.healthconnectjetpackcompose.ui.theme.HealthConnectJetpackComposeTheme
import kotlinx.coroutines.launch

const val TAG = "Health Connect sample"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HealthConnectApp(healthConnectManager: HealthConnectManager) {
    HealthConnectJetpackComposeTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentRoute = navBackStackEntry?.destination?.route

        val availability by healthConnectManager.availability
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()

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
