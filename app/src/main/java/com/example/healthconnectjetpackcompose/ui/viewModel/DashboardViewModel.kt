package com.example.healthconnectjetpackcompose.ui.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectjetpackcompose.data.managers.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Instant
class DashboardViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    var permissionsGranted = mutableStateOf(false)
        private set
    var recordList: MutableState<List<HealthConnectData>> = mutableStateOf(listOf())
        private set
    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()
    fun initialLoad() {
        viewModelScope.launch {
        }
    }
}
data class HealthConnectData(
    val metaId : String,
    val value: String,
    val time: Instant?
)
class DashboardViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return DashboardViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
