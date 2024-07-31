package com.example.healthconnectjetpackcompose.model//package com.example.healthconnectjetpackcompose.model
//
//import android.content.Context
//import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
//import androidx.health.connect.client.records.BasalMetabolicRateRecord
//import androidx.health.connect.client.records.BloodPressureRecord
//import androidx.health.connect.client.records.BodyFatRecord
//import androidx.health.connect.client.records.BodyTemperatureRecord
//import androidx.health.connect.client.records.DistanceRecord
//import androidx.health.connect.client.records.HeartRateRecord
//import androidx.health.connect.client.records.HeightRecord
//import androidx.health.connect.client.records.OxygenSaturationRecord
//import androidx.health.connect.client.records.StepsRecord
//import androidx.health.connect.client.records.WeightRecord
//import androidx.health.connect.client.time.TimeRangeFilter
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.temporal.ChronoUnit
//import java.util.Calendar
//import kotlin.math.round
//
//class HealthConnectDataManager {
//    private suspend fun fetchHealthConnectData(
//        healthConnectManager: HealthConnectManager,
//        metaId: String,
//        ctx: Context
//    ): Map<String, Pair<String, Long>> {
//        var today: Calendar = Calendar.getInstance()
//        val (startTime, endTime) = healthConnectManager.getStartAndEndOfADay(today)
//        val startInstant = Instant.ofEpochMilli(startTime)
//        val endInstant = Instant.ofEpochMilli(endTime)
//        var value: String = "-"
//        var timeStamp: Long = 0L
//        if (healthConnectManager.checkAvailability() == SDK_AVAILABLE) {
//            if (healthConnectManager.hasAllPermissions(HealthConnectManager.permissions)) {
//                when (metaId) {
//                    "body_temperature" -> {
//                        val record = healthConnectManager.readData<BodyTemperatureRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.temperature?.inCelsius?.toString() ?: "-"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
//
//                    "pulse" -> {
//                        val record = healthConnectManager.readData<HeartRateRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.samples?.lastOrNull()?.beatsPerMinute?.toString() ?: "-"
//                        timeStamp = record?.startTime?.toEpochMilli() ?: 0L
//                    }
//
//                    "blood_pressure" -> {
//                        val record = healthConnectManager.readData<BloodPressureRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value =
//                            "${record?.systolic?.inMillimetersOfMercury ?: 0.0}/${record?.diastolic?.inMillimetersOfMercury ?: 0.0}"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
//
//                    "spo2" -> {
//                        val record = healthConnectManager.readData<OxygenSaturationRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.percentage?.value?.toString() ?: "-"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
//
//                    "bmi" -> {
//                        val recordWeight = healthConnectManager.readData<WeightRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        val recordHeight = healthConnectManager.readData<HeightRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()?.height?.inMeters ?: 0.0
//                        if (recordHeight != 0.0 && recordWeight?.weight?.inKilograms != null) {
//                            value =
//                                (round((recordWeight.weight.inKilograms / (recordHeight * recordHeight)) * 10) / 10).toString()
//                            timeStamp = recordWeight.time.toEpochMilli() ?: 0L
//                        }
//                    }
//
//                    "weight" -> {
//                        val record = healthConnectManager.readData<WeightRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.weight?.inKilograms?.toString() ?: "-"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
//
//                    "weight_diff" -> {
//                        val records = healthConnectManager.readData<WeightRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant)
//                        )
//                        if (records.size > 1) {
//                            value =
//                                (records.lastOrNull()!!.weight.inKilograms - records[records.size - 2].weight.inKilograms).toString()
//                            timeStamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
//                        }
//
//
//                    }
//
//                    "steps" -> {
//                        val records = healthConnectManager.readData<StepsRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        )
//                        var sumVal = 0L
//                        for (item in records) {
//                            sumVal += item.count ?: 0L
//                        }
//                        value = sumVal.toDouble().toString()
//                        timeStamp = records.lastOrNull()?.startTime?.toEpochMilli() ?: 0L
//                    }
//
//                    "walk_time" -> {
//                        val records = healthConnectManager.readData<StepsRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant)
//                        )
//                        value = calcWalkTime(records).toString()
//                        timeStamp = records.lastOrNull()?.startTime?.toEpochMilli() ?: 0L
//                    }
//
//                    "walk_distance" -> {
//                        val records = healthConnectManager.readData<DistanceRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        )
//                        var sumVal = 0.0
//                        for (item in records) {
//                            sumVal += item.distance.inKilometers ?: 0.0
//                        }
//                        value = ((round(sumVal * 100)) / 100).toString()
//                        timeStamp = records.lastOrNull()?.startTime?.toEpochMilli() ?: 0L
//                    }
//
//                    "body_fat" -> {
//                        val record = healthConnectManager.readData<BodyFatRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.percentage?.value?.toString() ?: "-"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
//
//                    "bmr" -> {
//                        val record = healthConnectManager.readData<BasalMetabolicRateRecord>(
//                            TimeRangeFilter.between(startInstant, endInstant),
//                        ).lastOrNull()
//                        value = record?.basalMetabolicRate?.inKilocaloriesPerDay.toString() ?: "-"
//                        timeStamp = record?.time?.toEpochMilli() ?: 0L
//                    }
////
//                    else -> {
//                        value = "-"
//                        timeStamp = 0L
//                    }
//                }
//            }
//        }
//        return mapOf(metaId to Pair(value, timeStamp))
//    }
//
//    private fun calcWalkTime(dataSet: List<StepsRecord>): Double {
//        val hourAverages = mutableMapOf<LocalDateTime, Double>()
//        for (stepsRecord in dataSet) {
//            val tvValue = 1.0
//
//            val activityDateTime =
//                LocalDateTime.ofInstant(stepsRecord.startTime, ZoneId.of("Asia/Tokyo"))
//                    .truncatedTo(ChronoUnit.HOURS)
//            if (activityDateTime != null) {
//                hourAverages[activityDateTime] =
//                    hourAverages.getOrDefault(activityDateTime, 0.0) + tvValue
//            }
//        }
//        var mValue = 0.0
//        for (eachHour in hourAverages) {
//            mValue = mValue.plus(eachHour.value)
//        }
//        return mValue
//    }
//}