package com.example.healthconnectjetpackcompose.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.healthconnectjetpackcompose.R
import com.example.healthconnectjetpackcompose.ui.view.components.HealthCard
import com.example.healthconnectjetpackcompose.ui.viewModel.HealthConnectData

@Composable
fun DashboardScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    onPermissionsLaunch: (Set<String>) -> Unit = {},
    recordList: List<HealthConnectData>
) {
    Scaffold { innerPadding ->
        // Apply background color to the entire screen if needed
        Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Set 2 columns
                contentPadding = PaddingValues(16.dp), // Padding around the grid
                verticalArrangement = Arrangement.spacedBy(16.dp), // Space between rows
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between columns
            ) {
                items(recordList) { record ->
                    HealthCard(
                        icon = R.drawable.ic_body_temparature,
                        title = record.metaId,
                        latestData = record.value,
                        latestDate = record.time?.toString() ?: "-",
                        cardWidth = Modifier.fillMaxWidth().width(150.dp), // Adjust width as needed
                        modifier = Modifier
                            .background(color = Color.LightGray, shape = RoundedCornerShape(20.dp))

                    )
                }
            }
        }

        if (!permissionsGranted) {
            onPermissionsLaunch(permissions)
        }
    }
}
