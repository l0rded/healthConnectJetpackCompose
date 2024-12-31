package com.example.healthconnectjetpackcompose.data.managers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
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
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.healthconnectjetpackcompose.domain.model.dataclass.HealthConnectAppInfo
import com.example.healthconnectjetpackcompose.domain.model.enums.ChartRange
import java.time.Instant
import java.util.Calendar

class HealthConnectManager(private val context: Context) {
    val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val healthConnectCompatibleApps by lazy {
        val intent = Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")

        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            context.packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_ALL,
            )
        }

        packages.associate {
            val icon = try {
                context.packageManager.getApplicationIcon(it.activityInfo.packageName)
            } catch (e: NotFoundException) {
                null
            }
            val label =
                context.packageManager.getApplicationLabel(it.activityInfo.applicationInfo)
                    .toString()
            it.activityInfo.packageName to HealthConnectAppInfo(
                packageName = it.activityInfo.packageName, icon = icon, appLabel = label,
            )
        }
    }

    fun checkAvailability(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(Permission.permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun revokeAllPermissions() {
        healthConnectClient.permissionController.revokeAllPermissions()
    }

    suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter, dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter,
        )
        return healthConnectClient.readRecords(request).records
    }


    fun getStartAndEndOfADayInstant(cal: Calendar): Pair<Instant, Instant> {
        val tmpCal = cal.clone() as Calendar
        tmpCal[Calendar.HOUR_OF_DAY] = 0
        tmpCal[Calendar.MINUTE] = 0
        tmpCal[Calendar.SECOND] = 0
        tmpCal[Calendar.MILLISECOND] = 0
        val startTime = tmpCal.toInstant()
        tmpCal[Calendar.HOUR_OF_DAY] = 23
        tmpCal[Calendar.MINUTE] = 59
        tmpCal[Calendar.SECOND] = 59
        tmpCal[Calendar.MILLISECOND] = 999
        val endTime = tmpCal.toInstant()
        return startTime to endTime
    }

    fun getStartAndEndOfADay(cal: Calendar): Pair<Long, Long> {
        val tmpCal = cal.clone() as Calendar
        tmpCal[Calendar.HOUR_OF_DAY] = 0
        tmpCal[Calendar.MINUTE] = 0
        tmpCal[Calendar.SECOND] = 0
        tmpCal[Calendar.MILLISECOND] = 0
        val startTime = tmpCal.timeInMillis
        tmpCal[Calendar.HOUR_OF_DAY] = 23
        tmpCal[Calendar.MINUTE] = 59
        tmpCal[Calendar.SECOND] = 59
        tmpCal[Calendar.MILLISECOND] = 999
        val endTime = tmpCal.timeInMillis
        return startTime to endTime
    }

    suspend fun insertRecord(records: List<Record>) {
        try {
            healthConnectClient.insertRecords(records)
        } catch (e: Exception) {
            println("Error inserting records: ${e.message}")
        }
    }
    object Utility {
        fun calculatePeriod(
            chartRange: ChartRange,
            calendar: Calendar = Calendar.getInstance(),
        ): Pair<Instant, Instant> {
            val calendarClone = calendar.clone() as Calendar
            calendarClone.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            when (chartRange) {
                ChartRange.DAILY -> Unit
                ChartRange.WEEKLY -> calendarClone.set(
                    Calendar.DAY_OF_WEEK,
                    calendarClone.firstDayOfWeek + 6,
                )

                ChartRange.MONTHLY -> calendarClone.set(
                    Calendar.DAY_OF_MONTH,
                    calendarClone.getActualMaximum(Calendar.DAY_OF_MONTH),
                )

                ChartRange.YEARLY -> calendarClone.set(
                    Calendar.DAY_OF_YEAR,
                    calendarClone.getActualMaximum(Calendar.DAY_OF_YEAR),
                )

                ChartRange.ALL -> calendarClone.set(
                    Calendar.DAY_OF_YEAR,
                    calendarClone.getActualMaximum(Calendar.DAY_OF_YEAR),
                )
            }

            val end = calendarClone.toInstant()

            calendarClone.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when (chartRange) {
                ChartRange.DAILY -> Unit
                ChartRange.WEEKLY -> calendarClone.set(
                    Calendar.DAY_OF_WEEK,
                    calendarClone.firstDayOfWeek,
                )

                ChartRange.MONTHLY -> calendarClone.set(Calendar.DAY_OF_MONTH, 1)
                ChartRange.YEARLY -> calendarClone.set(Calendar.DAY_OF_YEAR, 1)
                ChartRange.ALL -> calendarClone.add(Calendar.YEAR, -11) // Handle custom periods
            }
            val start = calendarClone.toInstant()
            return Pair(start, end)
        }
    }


    object Permission {
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

            HealthPermission.getWritePermission(DistanceRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),

            HealthPermission.getWritePermission(FloorsClimbedRecord::class),
            HealthPermission.getReadPermission(FloorsClimbedRecord::class),

            HealthPermission.getWritePermission(BloodGlucoseRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),

            HealthPermission.getWritePermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        )
    }

    object MetaId {
        const val PULSE = "pulse"
        const val STEPS = "steps"
        const val WALK_TIME = "walk_time"
        const val WALK_DISTANCE = "walk_distance"
        const val STAIRS_CLIMBED = "stairs_climbed"
        const val BODY_TEMPERATURE = "body_temparature"
        const val WEIGHT = "weight"
        const val WEIGHT_PREV = "weight_prev"
        const val BODY_FAT = "body_fat"
        const val BASAL_METABOLISM = "basal_metabolism"
        const val BLOOD_PRESSURE = "blood_pressure"
        const val SPO2 = "spo2"
        const val BLOOD_GLUCOSE = "blood_glucose"
        const val LBM = "lbm"

        val aggregateMetaIdSets = setOf(
            STEPS,
            WALK_TIME,
            WALK_DISTANCE,
            STAIRS_CLIMBED,
        )
        val metaIdSet = setOf(
            STEPS,
            WALK_TIME,
            WALK_DISTANCE,
            STAIRS_CLIMBED,
            PULSE,
            BODY_TEMPERATURE,
            WEIGHT,
            WEIGHT_PREV,
            BODY_FAT,
            BASAL_METABOLISM,
            BLOOD_PRESSURE,
            SPO2,
            BLOOD_GLUCOSE,
            LBM,
        )
        val sumValueMetaIdSets = setOf(
            STEPS,
            WALK_TIME,
            WALK_DISTANCE,)

    }
}