package com.example.healthconnectjetpackcompose.navigation

import com.example.healthconnectjetpackcompose.R

enum class Screen(val route: String, val titleId: Int, val hasMenuItem: Boolean = true) {
    DashboardScreen("dashboard_screen", R.string.dashboard_screen, false)
}