package com.example.healthconnectjetpackcompose.viewModel

import android.os.RemoteException
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectjetpackcompose.model.HealthConnectManager
import com.example.healthconnectjetpackcompose.viewModel.DashboardViewModel.MetaId.metaIdSet
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.round

class DashboardViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    object MetaId {
        const val BODY_TEMPERATURE = "body_temparature"
        const val WEIGHT = "weight"
        const val WEIGHT_PREV = "weight_prev"
        const val BODY_FAT = "body_fat"
        const val BASAL_METABOLISM = "basal_metabolism"
        const val BMI = "bmi"
        const val BLOOD_PRESSURE = "blood_pressure"
        const val PULSE = "pulse"
        const val SPO2 = "spo2"
        const val STEPS = "steps"
        const val WALK_TIME = "walk_time"
        const val WALK_DISTANCE = "walk_distance"
        const val STAIRS_CLIMBED = "stairs_climbed"
        const val BLOOD_GLUCOSE = "blood_glucose"
        const val LBM = "lbm"

        val metaIdSet = setOf(
            BODY_TEMPERATURE,
            WEIGHT,
            WEIGHT_PREV,
            BODY_FAT,
            BASAL_METABOLISM,
            BMI,
            BLOOD_PRESSURE,
            PULSE,
            SPO2,
            STEPS,
            WALK_TIME,
            WALK_DISTANCE,
            STAIRS_CLIMBED,
            BLOOD_GLUCOSE,
            LBM
        )
    }

    var permissionsGranted = mutableStateOf(false)
        private set
    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set
    var recordList: MutableState<List<HealthConnectData>> = mutableStateOf(listOf())
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()
    fun initialLoad() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                getBodyTemperature()
            }
        }
    }

    private suspend fun getBodyTemperature() {
        for(item in metaIdSet){
            getLatestRecordValue(item)
        }
    }



private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
    permissionsGranted.value =
        healthConnectManager.hasAllPermissions(healthConnectManager.permissions)
    uiState = try {
        if (permissionsGranted.value) {
            block()
        }
        UiState.Done
    } catch (remoteException: RemoteException) {
        UiState.Error(remoteException)
    } catch (securityException: SecurityException) {
        UiState.Error(securityException)
    } catch (ioException: IOException) {
        UiState.Error(ioException)
    } catch (illegalStateException: IllegalStateException) {
        UiState.Error(illegalStateException)
    }
}
    private suspend fun getLatestRecordValue(
        metaId: String
    ) {
        val today: LocalDate = LocalDate.now()
        val start: Instant = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end: Instant = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        var value = 0.0
        var time: Instant? = null
        var bloodPressureVal = "-"
        when (metaId) {
            MetaId.BODY_TEMPERATURE -> {
                val records = healthConnectManager.readData<BodyTemperatureRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.temperature?.inCelsius ?: 0.0
                time = records.lastOrNull()?.time
            }

            MetaId.WEIGHT -> {
                val records = healthConnectManager.readData<WeightRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.weight?.inKilograms ?: 0.0
                time = records.lastOrNull()?.time
            }

            MetaId.WEIGHT_PREV -> {
                val records = healthConnectManager.readData<WeightRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value =
                    if (records.size > 1) {
                        (records.lastOrNull()!!.weight.inKilograms - records[records.size - 2].weight.inKilograms)
                    } else {
                        0.0
                    }
                time = records.lastOrNull()?.time
            }

            MetaId.BODY_FAT -> {
                val records = healthConnectManager.readData<BodyFatRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.percentage?.value ?: 0.0
                time = records.lastOrNull()?.time
            }

            MetaId.BASAL_METABOLISM -> {
                val records = healthConnectManager.readData<BasalMetabolicRateRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value =
                    records.lastOrNull()?.basalMetabolicRate?.inKilocaloriesPerDay ?: 0.0
                time = records.lastOrNull()?.time
            }
            MetaId.BLOOD_PRESSURE -> {
                val records = healthConnectManager.readData<BloodPressureRecord>(
                    TimeRangeFilter.between(start, end)
                )
                val sys =
                    round(
                        records.lastOrNull()?.systolic?.inMillimetersOfMercury ?: 0.0
                    ).toInt()
                val dys =
                    round(
                        records.lastOrNull()?.diastolic?.inMillimetersOfMercury ?: 0.0
                    ).toInt()
                if (sys != 0 && dys != 0) {
                    bloodPressureVal = "${sys}/${dys}"
                }
                time = records.lastOrNull()?.time
            }

            MetaId.PULSE -> {
                val records = healthConnectManager.readData<HeartRateRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value =
                    records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute?.toDouble() ?: 0.0
            }

            MetaId.SPO2 -> {
                val records = healthConnectManager.readData<OxygenSaturationRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.percentage?.value ?: 0.0
                time = records.lastOrNull()?.time
            }

            MetaId.STEPS -> {
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.count?.toDouble() ?: 0.0
                time = records.lastOrNull()?.startTime
            }

            MetaId.WALK_TIME -> {
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = calcWalkTime(records) ?: 0.0
                time = records.lastOrNull()?.startTime
            }

            MetaId.WALK_DISTANCE -> {
                val records = healthConnectManager.readData<DistanceRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.distance?.inKilometers ?: 0.0
                time = records.lastOrNull()?.startTime
            }

            MetaId.STAIRS_CLIMBED -> {
                val records = healthConnectManager.readData<FloorsClimbedRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.floors ?: 0.0
                time = records.lastOrNull()?.startTime
            }

            MetaId.BLOOD_GLUCOSE -> {
                val records = healthConnectManager.readData<BloodGlucoseRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.level?.inMillimolesPerLiter ?: 0.0
                time = records.lastOrNull()?.time
            }

            MetaId.LBM -> {
                val records = healthConnectManager.readData<LeanBodyMassRecord>(
                    TimeRangeFilter.between(start, end)
                )
                value = records.lastOrNull()?.mass?.inKilograms ?: 0.0
                time = records.lastOrNull()?.time
            }

            else -> value = 0.0
        }
        val finalValue = if (metaId != MetaId.BLOOD_PRESSURE) {
            if (value != 0.0) {
                (Math.round(value * 10) / 10.0).toString()
            } else {
                "-"
            }
        } else {
            bloodPressureVal
        }
        recordList.value+= HealthConnectData(metaId,finalValue,time)
//        recordList.value=mRecordList.value
    }
    private fun calcWalkTime(dataSet: List<StepsRecord>): Double {
        val hourAverages = mutableMapOf<LocalDateTime, Double>()
        for (stepsRecord in dataSet) {
            val tvValue = 1.0

            val activityDateTime =
                LocalDateTime.ofInstant(stepsRecord.startTime, ZoneId.of("Asia/Tokyo"))
                    .truncatedTo(ChronoUnit.HOURS)
            if (activityDateTime != null) {
                hourAverages[activityDateTime] =
                    hourAverages.getOrDefault(activityDateTime, 0.0) + tvValue
            }
        }
        var mValue = 0.0
        for (eachHour in hourAverages) {
            mValue = mValue.plus(eachHour.value)
        }
        return mValue
    }

sealed class UiState {
    data object Uninitialized : UiState()
    data object Done : UiState()
    data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
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
