package com.example.healthconnectjetpackcompose.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter

const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

//    val healthConnectCompatibleApps by lazy {
//        val intent = Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")
//
//        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            context.packageManager.queryIntentActivities(
//                intent,
//                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
//            )
//        } else {
//            context.packageManager.queryIntentActivities(
//                intent,
//                PackageManager.MATCH_ALL
//            )
//        }
//
//        packages.associate {
//            val icon = try {
//                context.packageManager.getApplicationIcon(it.activityInfo.packageName)
//            } catch (e: NotFoundException) {
//                null
//            }
//            val label = context.packageManager.getApplicationLabel(it.activityInfo.applicationInfo)
//                .toString()
//            it.activityInfo.packageName to
//                    HealthConnectAppInfo(
//                        packageName = it.activityInfo.packageName,
//                        icon = icon,
//                        appLabel = label
//                    )
//        }
//    }

    var availability = mutableStateOf(SDK_UNAVAILABLE)
        private set

    fun checkAvailability() {
        availability.value = HealthConnectClient.getSdkStatus(context)
    }

    init {
        checkAvailability()
    }

    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [PermissionController.createRequestPermissionResultContract].
     */
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun revokeAllPermissions() {
        healthConnectClient.permissionController.revokeAllPermissions()
    }

    suspend fun insertRecord(records: List<Record>) {
        try {
            healthConnectClient.insertRecords(records)
        } catch (e: Exception) {
            println("Error inserting records: ${e.message}")
        }
    }
    suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf()
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

   val permissions = setOf(
        HealthPermission.getWritePermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),

        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),

        HealthPermission.getWritePermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),

        HealthPermission.getWritePermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),

        HealthPermission.getWritePermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),

        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),

        HealthPermission.getWritePermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),

        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),

        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),

        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),

        HealthPermission.getWritePermission(FloorsClimbedRecord::class),
        HealthPermission.getReadPermission(FloorsClimbedRecord::class),

        HealthPermission.getWritePermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),

        HealthPermission.getWritePermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),

        HealthPermission.getWritePermission(HeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
    )


    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}

data class HealthConnectAppInfo(
    val packageName: String,
    val appLabel: String,
    val icon: Drawable?
)
