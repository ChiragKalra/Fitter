@file:OptIn(androidx.health.connect.client.ExperimentalDeduplicationApi::class)

package com.bruhascended.fitapp.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.ExperimentalDeduplicationApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseLap
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Modifier
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KClass

/**
 * One-shot export of Health Connect data that overlaps [localDate] in [zoneId], for debugging
 * (e.g. Samsung → HC nutrition shape). Only record types declared in [HealthConnectPermissions]
 * can be read.
 */
object HealthConnectDayDump {

    private const val TAG = "HealthConnectDayDump"
    private const val PAGE_SIZE = 5000

    /** Samsung Health package (typical); also filter [ORIGIN_SAMSUNG_SUBSTRING]. */
    const val SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth"

    private const val ORIGIN_SAMSUNG_SUBSTRING = "samsung"

    private val skipNutritionGetters = setOf(
        "getMetadata",
        "getStartTime",
        "getEndTime",
        "getStartZoneOffset",
        "getEndZoneOffset",
        "getName",
        "getMealType",
        "getClass",
    )

    fun outputFile(context: Context, localDate: LocalDate): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(base, "health_connect_exports").apply { mkdirs() }
        return File(dir, "health_connect_dump_$localDate.json")
    }

    suspend fun writeJsonForDay(
        context: Context,
        client: HealthConnectClient,
        localDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): File {
        val start = localDate.atStartOfDay(zoneId).toInstant()
        val endExclusive = localDate.plusDays(1).atStartOfDay(zoneId).toInstant()
        val range = TimeRangeFilter.between(start, endExclusive)

        val nutrition = client.readNutritionPaged(range)
        val steps = client.readAllPaged(StepsRecord::class, range)
        val distance = client.readAllPaged(DistanceRecord::class, range)
        val calories = client.readAllPaged(TotalCaloriesBurnedRecord::class, range)
        val exercise = client.readAllPaged(ExerciseSessionRecord::class, range)
        val weight = client.readAllPaged(WeightRecord::class, range)

        val root = JSONObject().apply {
            put("exportTool", "Fitter HealthConnectDayDump")
            put("localDate", localDate.toString())
            put("zoneId", zoneId.id)
            put("windowStartInclusive", start.toString())
            put("windowEndExclusive", endExclusive.toString())
            put(
                "note",
                "Rows include every origin (not only Samsung). " +
                    "Filter with metadata.dataOrigin.packageName or use originSummary / samsungFilteredTypes.",
            )
            put(
                "permissionsRequired",
                JSONArray(HealthConnectPermissions.readPermissions.sorted()),
            )
            put("types", JSONObject().apply {
                put("nutrition", JSONArray().apply { nutrition.forEach { put(it.toNutritionJson()) } })
                put("steps", JSONArray().apply { steps.forEach { put(it.toStepsJson()) } })
                put("distance", JSONArray().apply { distance.forEach { put(it.toDistanceJson()) } })
                put("totalCaloriesBurned", JSONArray().apply { calories.forEach { put(it.toCaloriesJson()) } })
                put("exerciseSession", JSONArray().apply { exercise.forEach { put(it.toExerciseJson()) } })
                put("weight", JSONArray().apply { weight.forEach { put(it.toWeightJson()) } })
            })
            put("originSummary", buildOriginSummary(nutrition, steps, distance, calories, exercise, weight))
            put(
                "samsungFilteredTypes",
                JSONObject().apply {
                    put("matchRule", "package == $SAMSUNG_HEALTH_PACKAGE OR package contains \"$ORIGIN_SAMSUNG_SUBSTRING\" (case-insensitive)")
                    put("nutrition", filterSamsung(nutrition) { it.toNutritionJson() })
                    put("steps", filterSamsung(steps) { it.toStepsJson() })
                    put("distance", filterSamsung(distance) { it.toDistanceJson() })
                    put("totalCaloriesBurned", filterSamsung(calories) { it.toCaloriesJson() })
                    put("exerciseSession", filterSamsung(exercise) { it.toExerciseJson() })
                    put("weight", filterSamsung(weight) { it.toWeightJson() })
                },
            )
        }

        val out = outputFile(context, localDate)
        out.writeText(root.toString(2))
        Log.i(TAG, "Wrote ${out.absolutePath} (${out.length()} bytes)")
        return out
    }

    private fun buildOriginSummary(
        nutrition: List<NutritionRecord>,
        steps: List<StepsRecord>,
        distance: List<DistanceRecord>,
        calories: List<TotalCaloriesBurnedRecord>,
        exercise: List<ExerciseSessionRecord>,
        weight: List<WeightRecord>,
    ): JSONObject =
        JSONObject().apply {
            put("nutrition", countsByPackage(nutrition) { it.metadata.dataOrigin.packageName })
            put("steps", countsByPackage(steps) { it.metadata.dataOrigin.packageName })
            put("distance", countsByPackage(distance) { it.metadata.dataOrigin.packageName })
            put("totalCaloriesBurned", countsByPackage(calories) { it.metadata.dataOrigin.packageName })
            put("exerciseSession", countsByPackage(exercise) { it.metadata.dataOrigin.packageName })
            put("weight", countsByPackage(weight) { it.metadata.dataOrigin.packageName })
        }

    private fun <T> countsByPackage(rows: List<T>, packageName: (T) -> String): JSONObject =
        rows.groupingBy(packageName).eachCount().entries
            .sortedByDescending { it.value }
            .fold(JSONObject()) { acc, e -> acc.put(e.key, e.value) }

    private fun <T : Any> filterSamsung(rows: List<T>, serialize: (T) -> JSONObject): JSONArray =
        JSONArray().apply {
            rows.filter { isSamsungOrigin(metadataPackage(it)) }.forEach { put(serialize(it)) }
        }

    private fun metadataPackage(any: Any): String =
        when (any) {
            is NutritionRecord -> any.metadata.dataOrigin.packageName
            is StepsRecord -> any.metadata.dataOrigin.packageName
            is DistanceRecord -> any.metadata.dataOrigin.packageName
            is TotalCaloriesBurnedRecord -> any.metadata.dataOrigin.packageName
            is ExerciseSessionRecord -> any.metadata.dataOrigin.packageName
            is WeightRecord -> any.metadata.dataOrigin.packageName
            else -> ""
        }

    private fun isSamsungOrigin(packageName: String): Boolean =
        packageName == SAMSUNG_HEALTH_PACKAGE ||
            packageName.contains(ORIGIN_SAMSUNG_SUBSTRING, ignoreCase = true)

    private suspend fun HealthConnectClient.readNutritionPaged(filter: TimeRangeFilter): List<NutritionRecord> {
        val out = mutableListOf<NutritionRecord>()
        var pageToken: String? = null
        do {
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = filter,
                dataOriginFilter = emptySet(),
                ascendingOrder = true,
                pageSize = PAGE_SIZE,
                pageToken = pageToken,
                deduplicateStrategy = 0,
            )
            val response = readRecords(request)
            out.addAll(response.records)
            pageToken = response.pageToken.takeUnless { it.isNullOrBlank() }
        } while (pageToken != null)
        return out
    }

    private suspend fun <T : Record> HealthConnectClient.readAllPaged(
        type: KClass<T>,
        filter: TimeRangeFilter,
    ): List<T> {
        val out = mutableListOf<T>()
        var pageToken: String? = null
        do {
            val request = ReadRecordsRequest(
                recordType = type,
                timeRangeFilter = filter,
                dataOriginFilter = emptySet(),
                ascendingOrder = true,
                pageSize = PAGE_SIZE,
                pageToken = pageToken,
            )
            val response = readRecords(request)
            @Suppress("UNCHECKED_CAST")
            out.addAll(response.records as List<T>)
            pageToken = response.pageToken.takeUnless { it.isNullOrBlank() }
        } while (pageToken != null)
        return out
    }

    private fun Metadata.toJson(): JSONObject =
        JSONObject().apply {
            put("id", id)
            put("clientRecordId", clientRecordId ?: JSONObject.NULL)
            put("clientRecordVersion", clientRecordVersion)
            put("recordingMethod", recordingMethod)
            put("lastModifiedTime", lastModifiedTime.toString())
            put("dataOrigin", JSONObject().put("packageName", dataOrigin.packageName))
            val d = device
            if (d != null) {
                put(
                    "device",
                    JSONObject().apply {
                        put("type", d.type)
                        put("manufacturer", d.manufacturer ?: JSONObject.NULL)
                        put("model", d.model ?: JSONObject.NULL)
                    },
                )
            } else {
                put("device", JSONObject.NULL)
            }
        }

    private fun NutritionRecord.toNutritionJson(): JSONObject =
        JSONObject().apply {
            putInterval(this@toNutritionJson)
            put("name", name ?: JSONObject.NULL)
            put("mealType", mealType)
            put("nutrients", collectNutrientsJson())
            put("metadata", metadata.toJson())
        }

    private fun NutritionRecord.collectNutrientsJson(): JSONObject {
        val jo = JSONObject()
        val methods = NutritionRecord::class.java.methods
        for (m in methods) {
            if (Modifier.isStatic(m.modifiers)) continue
            if (m.parameterCount != 0 || !m.name.startsWith("get")) continue
            if (m.name in skipNutritionGetters) continue
            val v = try {
                m.invoke(this)
            } catch (_: ReflectiveOperationException) {
                continue
            } ?: continue
            val key = m.name.removePrefix("get").replaceFirstChar { it.lowercaseChar() }
            when (v) {
                is Mass -> jo.put(key, v.inGrams)
                is Energy -> jo.put(key, v.inKilocalories)
                else -> { }
            }
        }
        return jo
    }

    private fun StepsRecord.toStepsJson(): JSONObject =
        JSONObject().apply {
            putInterval(this@toStepsJson)
            put("count", count)
            put("metadata", metadata.toJson())
        }

    private fun DistanceRecord.toDistanceJson(): JSONObject =
        JSONObject().apply {
            putInterval(this@toDistanceJson)
            put("distanceMeters", distance.inMeters)
            put("metadata", metadata.toJson())
        }

    private fun TotalCaloriesBurnedRecord.toCaloriesJson(): JSONObject =
        JSONObject().apply {
            putInterval(this@toCaloriesJson)
            put("energyKcal", energy.inKilocalories)
            put("metadata", metadata.toJson())
        }

    private fun WeightRecord.toWeightJson(): JSONObject =
        JSONObject().apply {
            put("time", time.toString())
            put("zoneOffset", zoneOffsetJson(zoneOffset))
            put("massKg", weight.inKilograms)
            put("metadata", metadata.toJson())
        }

    private fun ExerciseSessionRecord.toExerciseJson(): JSONObject =
        JSONObject().apply {
            putInterval(this@toExerciseJson)
            put("exerciseType", exerciseType)
            put("title", title ?: JSONObject.NULL)
            put("notes", notes ?: JSONObject.NULL)
            put("plannedExerciseSessionId", plannedExerciseSessionId ?: JSONObject.NULL)
            put("segments", JSONArray().apply { segments.forEach { put(it.toJson()) } })
            put("laps", JSONArray().apply { laps.forEach { put(it.toJson()) } })
            put("exerciseRouteResult", exerciseRouteResult?.toString() ?: JSONObject.NULL)
            put("metadata", metadata.toJson())
        }

    private fun ExerciseSegment.toJson(): JSONObject =
        JSONObject().apply {
            put("startTime", startTime.toString())
            put("endTime", endTime.toString())
            put("segmentType", segmentType)
            put("repetitions", repetitions)
        }

    private fun ExerciseLap.toJson(): JSONObject =
        JSONObject().apply {
            put("startTime", startTime.toString())
            put("endTime", endTime.toString())
            val len = length
            put("lengthMeters", if (len != null) len.inMeters else JSONObject.NULL)
        }

    private fun JSONObject.putInterval(record: StepsRecord) {
        put("startTime", record.startTime.toString())
        put("startZoneOffset", zoneOffsetJson(record.startZoneOffset))
        put("endTime", record.endTime.toString())
        put("endZoneOffset", zoneOffsetJson(record.endZoneOffset))
    }

    private fun JSONObject.putInterval(record: DistanceRecord) {
        put("startTime", record.startTime.toString())
        put("startZoneOffset", zoneOffsetJson(record.startZoneOffset))
        put("endTime", record.endTime.toString())
        put("endZoneOffset", zoneOffsetJson(record.endZoneOffset))
    }

    private fun JSONObject.putInterval(record: TotalCaloriesBurnedRecord) {
        put("startTime", record.startTime.toString())
        put("startZoneOffset", zoneOffsetJson(record.startZoneOffset))
        put("endTime", record.endTime.toString())
        put("endZoneOffset", zoneOffsetJson(record.endZoneOffset))
    }

    private fun JSONObject.putInterval(record: ExerciseSessionRecord) {
        put("startTime", record.startTime.toString())
        put("startZoneOffset", zoneOffsetJson(record.startZoneOffset))
        put("endTime", record.endTime.toString())
        put("endZoneOffset", zoneOffsetJson(record.endZoneOffset))
    }

    private fun JSONObject.putInterval(record: NutritionRecord) {
        put("startTime", record.startTime.toString())
        put("startZoneOffset", zoneOffsetJson(record.startZoneOffset))
        put("endTime", record.endTime.toString())
        put("endZoneOffset", zoneOffsetJson(record.endZoneOffset))
    }

    private fun zoneOffsetJson(zone: ZoneOffset?): Any =
        zone?.toString() ?: JSONObject.NULL
}
