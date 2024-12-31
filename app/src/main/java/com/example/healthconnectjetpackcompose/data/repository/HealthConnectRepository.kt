package com.example.healthconnectjetpackcompose.data.repository


import android.content.Context
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.BloodGlucose
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Power
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.Temperature
import com.example.healthconnectjetpackcompose.data.managers.HealthConnectManager
import com.example.healthconnectjetpackcompose.data.managers.HealthConnectManager.MetaId
import com.example.healthconnectjetpackcompose.utility.saveStringAsFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.round
/**
 *  HealthConnectRepositoryClass for fetching and caching category data.
 *  @param healthConnectManager The HealthConnectManager instance for making API calls.
 *  @param context
 *  @author Firdaus Abdullah
 *  @since 1.0.0
 */
class HealthConnectRepository(
    private val healthConnectManager: HealthConnectManager,
    private val context: Context,
) {
    /**
     * Update HealthConnect Data
     */
    suspend fun updateHealthConnectData(
        start: Instant, value: String, metaId: String,
    ) {
        val records = mutableListOf<Record>()
        val offset = start.atZone(ZoneId.systemDefault()).offset
        var mValue = 0.0
        if (metaId != "blood_pressure") {
            mValue = value.toDouble()
        }
        try {
            when (metaId) {
                MetaId.BODY_TEMPERATURE -> {
                    records.add(
                        BodyTemperatureRecord(
                            start,
                            offset,
                            Temperature.celsius(mValue),
                            BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_UNKNOWN,
                            Metadata(),
                        ),
                    )
                }

                MetaId.WEIGHT -> {
                    records.add(
                        WeightRecord(
                            start, offset, Mass.kilograms(mValue), Metadata(),
                        ),
                    )
                }

                MetaId.BODY_FAT -> {
                    records.add(
                        BodyFatRecord(
                            start, offset, Percentage(mValue), Metadata(),
                        ),
                    )
                }

              MetaId.BASAL_METABOLISM -> {
                    records.add(
                        BasalMetabolicRateRecord(
                            start, offset, Power.kilocaloriesPerDay(mValue), Metadata(),
                        ),
                    )
                }

                MetaId.BLOOD_PRESSURE-> {
                    val pressureValue = value.split("|")
                    records.add(
                        BloodPressureRecord(
                            start,
                            offset,
                            Pressure.millimetersOfMercury(pressureValue[0].toDouble()),
                            Pressure.millimetersOfMercury(pressureValue[1].toDouble()),
                            0,
                            0,
                            Metadata(),
                        ),
                    )
                }

                MetaId.PULSE -> {
                    records.add(
                        HeartRateRecord(
                            start, offset, start.plusSeconds(1), offset,
                            listOf(
                                HeartRateRecord.Sample(start, mValue.toLong()),
                            ),
                            Metadata(),
                        ),
                    )
                }

                MetaId.SPO2 -> {
                    records.add(
                        OxygenSaturationRecord(
                            start, offset, Percentage(mValue), Metadata(),
                        ),
                    )
                }

                MetaId.STEPS -> {
                    records.add(
                        StepsRecord(
                            start,
                            offset,
                            start.plusSeconds(1),
                            offset,
                            mValue.toLong(),
                            Metadata(),
                        ),
                    )
                }

                MetaId.WALK_TIME -> {
                    records.add(
                        BasalMetabolicRateRecord(
                            start, offset, Power.kilocaloriesPerDay(mValue), Metadata(),
                        ),
                    )
                }

                MetaId.WALK_DISTANCE -> {
                    records.add(
                        DistanceRecord(
                            start,
                            offset,
                            start.plusSeconds(1),
                            offset,
                            Length.kilometers(mValue),
                            Metadata(),
                        ),
                    )
                }

                MetaId.STAIRS_CLIMBED -> {
                    records.add(
                        FloorsClimbedRecord(
                            start, offset, start.plusSeconds(1), offset, mValue, Metadata(),
                        ),
                    )
                }

                MetaId.BLOOD_GLUCOSE -> {
                    records.add(
                        BloodGlucoseRecord(
                            start,
                            offset,
                            BloodGlucose.millimolesPerLiter(mValue),
                            0,
                            0,
                            0,
                            Metadata(),
                        ),
                    )
                }

               MetaId.LBM-> {
                    records.add(
                        LeanBodyMassRecord(
                            start, offset, Mass.kilograms(mValue), Metadata(),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
//            ToastUtils.show(
//                e.message.toString(),
//            )
        }
        healthConnectManager.insertRecord(records)
    }

    /**
     * Get Record Value from HealthConnect
     */
    suspend fun getRecordValue(
        start: Instant, end: Instant, metaId: String,
    ): Pair<String, Long> {
        var value = 0.0
        var timestamp = 0L
        var bloodPressureVal = ""
        if (healthConnectManager.hasAllPermissions()) {
            when (metaId) {
                MetaId.BODY_TEMPERATURE -> {
                    val records = healthConnectManager.readData<BodyTemperatureRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.temperature?.inCelsius ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.WEIGHT -> {
                    val records = healthConnectManager.readData<WeightRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.weight?.inKilograms ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.WEIGHT_PREV -> {
                    val records = healthConnectManager.readData<WeightRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = if (records.size > 1) {
                        (records.lastOrNull()!!.weight.inKilograms - records[records.size - 2].weight.inKilograms)
                    } else {
                        0.0
                    }
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.BODY_FAT -> {
                    val records = healthConnectManager.readData<BodyFatRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.percentage?.value ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }
                MetaId.BASAL_METABOLISM -> {
                    val records = healthConnectManager.readData<BasalMetabolicRateRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.basalMetabolicRate?.inKilocaloriesPerDay ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }
                MetaId.BLOOD_PRESSURE -> {
                    val records = healthConnectManager.readData<BloodPressureRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    val sys = round(
                        records.lastOrNull()?.systolic?.inMillimetersOfMercury ?: 0.0,
                    ).toInt()
                    val dys = round(
                        records.lastOrNull()?.diastolic?.inMillimetersOfMercury ?: 0.0,
                    ).toInt()
                    if (sys != 0 && dys != 0) {
                        bloodPressureVal = "${sys}|${dys}"
                    }
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.PULSE -> {
                    val records = healthConnectManager.readData<HeartRateRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value =
                        records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute?.toDouble()
                            ?: 0.0
                    timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                }

                MetaId.SPO2 -> {
                    val records = healthConnectManager.readData<OxygenSaturationRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.percentage?.value ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.STEPS -> {
                    val records = healthConnectManager.readData<StepsRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.count?.toDouble() ?: 0.0
                    timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                }

                MetaId.WALK_TIME -> {
                    val records = healthConnectManager.readData<StepsRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = calculateWalkTime(records)
                    timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                }

                MetaId.WALK_DISTANCE -> {
                    val records = healthConnectManager.readData<DistanceRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.distance?.inKilometers ?: 0.0
                    timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                }

                MetaId.STAIRS_CLIMBED -> {
                    val records = healthConnectManager.readData<FloorsClimbedRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.floors ?: 0.0
                    timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                }

                MetaId.BLOOD_GLUCOSE -> {
                    val records = healthConnectManager.readData<BloodGlucoseRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.level?.inMillimolesPerLiter ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                MetaId.LBM -> {
                    val records = healthConnectManager.readData<LeanBodyMassRecord>(
                        TimeRangeFilter.between(start, end),
                    )
                    value = records.lastOrNull()?.mass?.inKilograms ?: 0.0
                    timestamp = records.lastOrNull()?.time?.toEpochMilli() ?: 0L
                }

                else -> value = 0.0
            }
        }
        val finalValue = if (metaId != MetaId.BLOOD_PRESSURE) {
            if (value != 0.0) {
                (Math.round(value * 10) / 10.0).toString()
            } else {
                ""
            }
        } else {
            bloodPressureVal
        }
        val data = Pair(finalValue, timestamp)
        saveDataToCache(metaId, start,end, data)
        return data
    }

    /**
     * Get Aggregate Record Value from HealthConnect
     */
    suspend fun getAggregateRecordValue(
        start: Instant, end: Instant, metaId: String,
    ): Pair<String, Long> {
        var value = 0.0
        var timestamp= 0L
        if (healthConnectManager.checkAvailability() == SDK_AVAILABLE) {
            if (healthConnectManager.hasAllPermissions()) {
                when (metaId) {
                    MetaId.STEPS -> {
                        val records = healthConnectManager.readData<StepsRecord>(
                            TimeRangeFilter.between(start, end),
                        )
                        var sumVal = 0L
                        for (item in records) {
                            sumVal += item.count
                        }
                        value = sumVal.toDouble()
                        timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                    }

                    MetaId.STAIRS_CLIMBED -> {
                        val records = healthConnectManager.readData<FloorsClimbedRecord>(
                            TimeRangeFilter.between(start, end),
                        )
                        var sumVal = 0.0
                        for (item in records) {
                            sumVal += item.floors
                        }
                        value = sumVal
                        timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                    }

                    MetaId.WALK_TIME -> {
                        val records = healthConnectManager.readData<StepsRecord>(
                            TimeRangeFilter.between(start, end),
                        )
                        value = calculateWalkTime(records)
                        timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                    }

                    MetaId.WALK_DISTANCE -> {
                        val records = healthConnectManager.readData<DistanceRecord>(
                            TimeRangeFilter.between(start, end),
                        )
                        var sumVal = 0.0
                        for (item in records) {
                            sumVal += item.distance.inKilometers
                        }
                        value = ((round(sumVal * 100)) / 100)
                        timestamp = records.lastOrNull()?.endTime?.toEpochMilli() ?: 0L
                    }
                }
            }
        }
        val finalValue = Math.round(value).toString()
        val data = Pair(finalValue, timestamp)
        saveDataToCache(metaId, start,end, data)
        return data
    }

    /**
     * Get Range Value from HealthConnect
     */
    suspend fun getRecordValuesRangeInList(
        start: Instant,
        end: Instant,
        metaId: String,
    ): List<Pair<Double, Long>> {
        val results = mutableListOf<Pair<Double, Long>>()

        when (metaId) {
            MetaId.BODY_TEMPERATURE -> {
                val records = healthConnectManager.readData<BodyTemperatureRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.temperature.inCelsius, record.time.toEpochMilli()))
                }
            }

            MetaId.WEIGHT -> {
                val records = healthConnectManager.readData<WeightRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.weight.inKilograms, record.time.toEpochMilli()))
                }
            }

            MetaId.WEIGHT_PREV -> {
                val records = healthConnectManager.readData<WeightRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (i in 1 until records.size) {
                    val diff = records[i].weight.inKilograms - records[i - 1].weight.inKilograms
                    results.add(Pair(diff, records[i].time.toEpochMilli()))
                }
            }

            MetaId.BODY_FAT -> {
                val records = healthConnectManager.readData<BodyFatRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.percentage.value, record.time.toEpochMilli()))
                }
            }

            MetaId.BLOOD_PRESSURE -> {
                val records = healthConnectManager.readData<BloodPressureRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    val sys = round(record.systolic.inMillimetersOfMercury)
                    val dia = round(record.diastolic.inMillimetersOfMercury)
                    if (sys != 0.0 && dia != 0.0) {
                        results.add(Pair(dia, record.time.toEpochMilli()))
                    }
                }
            }

            MetaId.BASAL_METABOLISM -> {
                val records = healthConnectManager.readData<BasalMetabolicRateRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(
                        Pair(
                            record.basalMetabolicRate.inKilocaloriesPerDay,
                            record.time.toEpochMilli(),
                        ),
                    )
                }
            }

            MetaId.PULSE -> {
                val records = healthConnectManager.readData<HeartRateRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    for (sample in record.samples) {
                        results.add(
                            Pair(
                                sample.beatsPerMinute.toDouble(), record.startTime.toEpochMilli(),
                            ),
                        )
                    }
                }
            }

            MetaId.SPO2 -> {
                val records = healthConnectManager.readData<OxygenSaturationRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.percentage.value, record.time.toEpochMilli()))
                }
            }

            MetaId.STEPS -> {
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.count.toDouble(), record.startTime.toEpochMilli()))
                }
            }

            MetaId.WALK_TIME -> {
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start, end),
                )
                val mRecords = calculateWalkTimeInList(records)
                for (record in mRecords) {
                    results.add(Pair(record.first, record.second))
                }
            }

            MetaId.WALK_DISTANCE -> {
                val records = healthConnectManager.readData<DistanceRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.distance.inKilometers, record.startTime.toEpochMilli()))
                }
            }

            MetaId.STAIRS_CLIMBED -> {
                val records = healthConnectManager.readData<FloorsClimbedRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.floors, record.startTime.toEpochMilli()))
                }
            }

            MetaId.BLOOD_GLUCOSE -> {
                val records = healthConnectManager.readData<BloodGlucoseRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.level.inMillimolesPerLiter, record.time.toEpochMilli()))
                }
            }

            MetaId.LBM -> {
                val records = healthConnectManager.readData<LeanBodyMassRecord>(
                    TimeRangeFilter.between(start, end),
                )
                for (record in records) {
                    results.add(Pair(record.mass.inKilograms, record.time.toEpochMilli()))
                }
            }
        }
        return results
    }

    /**
     * Calculate Walk Time
     */
    private fun calculateWalkTime(dataSet: List<StepsRecord>): Double {
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
    private fun calculateWalkTimeInList(dataSet: List<StepsRecord>): List<Pair<Double, Long>> {
        val hourAverages = mutableMapOf<Long, Double>()
        for (stepsRecord in dataSet) {
            val tvValue = 1.0

            val activityDateTime =
                LocalDateTime.ofInstant(stepsRecord.startTime, ZoneId.of("Asia/Tokyo"))
                    .truncatedTo(ChronoUnit.HOURS)
            val activityMillis =
                activityDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            hourAverages[activityMillis] = hourAverages.getOrDefault(activityMillis, 0.0) + tvValue
        }

        val mList = mutableListOf<Pair<Double, Long>>()
        for (eachHour in hourAverages) {
            mList.add(Pair(eachHour.value, eachHour.key))
        }
        return mList
    }

    /**
     * Load category data from cache directory if it exists.
     */
    private fun loadDataFromCache(start: Instant,end:Instant, metaId: String,isGraph: Boolean?=false): Pair<String, Long>? {
        val cacheDir = context.cacheDir
        val fileName = if(isGraph==true) generateFileName(metaId, start,end)else generateFileName(metaId, start)
        val file = File(cacheDir, fileName)

        return if (file.exists()) {
            try {
                val json = file.readText()
                val gson = Gson()
                val type = object : TypeToken<Pair<String, Long>>() {}.type
                return gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if an error occurs while reading the file
            }
        } else {
            null // Return null if no cache file exists
        }
    }


    /**
     * Save new category data to cache directory.
     */
    private fun saveDataToCache(metaId: String, start: Instant,end: Instant, data: Pair<String, Long>,isGraph: Boolean?=false) {
        val cacheDir = context.cacheDir
        val fileName = if(isGraph==true) generateFileName(metaId, start,end)else generateFileName(metaId, start)
        val json = Gson().toJson(data)
        saveStringAsFile(json,fileName,cacheDir)
    }

    /**
     * Generates a cache file name based on category and date.
     */
    private fun generateFileName(metaId: String, date: Instant): String {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toEpochMilli()
        val timestamp = formatter.format(calendar.time)
        return "healthConnect_${metaId}_$timestamp.json"
    }
    private fun generateFileName(metaId: String, start: Instant,end: Instant): String {
        val formatter=
            SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start.toEpochMilli()
        val startTime = formatter.format(calendar.time)
        calendar.timeInMillis = end.toEpochMilli()
        val endTime = formatter.format(calendar.time)
        return "healthConnect_${metaId}_${startTime}_$endTime.json"
    }

    /**
     * fetch data from cache
     */
    fun fetchDataFromCache(start: Date,end:Date ,metaId: String): Pair<String, Long> {
        val cachedData = loadDataFromCache(start.toInstant(), end.toInstant(),metaId)
        return cachedData?: Pair("", 0L)
    }
}