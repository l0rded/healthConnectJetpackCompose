package com.example.healthconnectjetpackcompose.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnectjetpackcompose.model.HealthConnectManager
import com.example.healthconnectjetpackcompose.view.screens.DashboardScreen
import com.example.healthconnectjetpackcompose.viewModel.DashboardViewModel
import com.example.healthconnectjetpackcompose.viewModel.DashboardViewModelFactory

/**
 * Provides the navigation in the app.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DashboardScreen.route
    ) {
        composable(Screen.DashboardScreen.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(
                    healthConnectManager = healthConnectManager
                )
            )
            val permissionsGranted by viewModel.permissionsGranted
            val permissions = healthConnectManager.permissions
            val onPermissionsResult = {viewModel.initialLoad()}
            val recordList by viewModel.recordList
            val permissionsLauncher =
                rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
                    onPermissionsResult()}
            DashboardScreen(
                permissionsGranted = permissionsGranted,
                permissions = permissions,
                onPermissionsLaunch = { values ->
                    permissionsLauncher.launch(values)},
                recordList = recordList
            )
        }
    }
}
