package com.example.healthconnectjetpackcompose

// Import statements
import android.os.RemoteException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Pressure
import com.example.healthconnectjetpackcompose.model.HealthConnectManager
import com.example.healthconnectjetpackcompose.viewModel.DashboardViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.io.IOException
import java.time.Instant


//This class cannot be run because Health Connect must be test on a physical phone that has Health Connect app inside them
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var healthConnectManager: HealthConnectManager

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        viewModel = DashboardViewModel(healthConnectManager)
    }

    @Test
    fun `initialLoad should update recordList with fetched data`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(true)
        `when`(healthConnectManager.readData<BloodPressureRecord>(any(TimeRangeFilter::class.java)))
            .thenReturn(listOf(BloodPressureRecord(Instant.now(), null, Pressure.millimetersOfMercury(120.0), Pressure.millimetersOfMercury(80.0))))

        // When
        viewModel.initialLoad()

        // Then
        val records = viewModel.recordList.value
        assertEquals(1, records.size)
        assertEquals(DashboardViewModel.MetaId.BLOOD_PRESSURE, records[0].metaId)
        assertEquals("120/80", records[0].value)
    }

    @Test
    fun `initialLoad should handle permissions not granted`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(false)

        // When
        viewModel.initialLoad()

        // Then
        assertEquals(DashboardViewModel.UiState.Done, viewModel.uiState)
        assertEquals(0, viewModel.recordList.value.size)
    }

    @Test
    fun `initialLoad should handle RemoteException`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(true)
        `when`(healthConnectManager.readData<BloodPressureRecord>(any(TimeRangeFilter::class.java)))
            .thenThrow(RemoteException())

        // When
        viewModel.initialLoad()

        // Then
        assert(viewModel.uiState is DashboardViewModel.UiState.Error)
    }

    @Test
    fun `initialLoad should handle SecurityException`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(true)
        `when`(healthConnectManager.readData<BloodPressureRecord>(any(TimeRangeFilter::class.java)))
            .thenThrow(SecurityException())

        // When
        viewModel.initialLoad()

        // Then
        assert(viewModel.uiState is DashboardViewModel.UiState.Error)
    }

    @Test
    fun `initialLoad should handle IOException`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(true)
        `when`(healthConnectManager.readData<BloodPressureRecord>(any(TimeRangeFilter::class.java)))
            .thenThrow(IOException())

        // When
        viewModel.initialLoad()

        // Then
        assert(viewModel.uiState is DashboardViewModel.UiState.Error)
    }

    @Test
    fun `initialLoad should handle IllegalStateException`() = runBlockingTest {
        // Given
        `when`(healthConnectManager.hasAllPermissions(healthConnectManager.permissions)).thenReturn(true)
        `when`(healthConnectManager.readData<BloodPressureRecord>(any(TimeRangeFilter::class.java)))
            .thenThrow(IllegalStateException())

        // When
        viewModel.initialLoad()

        // Then
        assert(viewModel.uiState is DashboardViewModel.UiState.Error)
    }
}
