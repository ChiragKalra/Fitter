package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.activity.types.ActivityType
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.reflect.KClass

object HealthConnectActivitySync {

    private const val TAG = "HealthConnectActivity"

    data class ImportResult(
        val dayEntries: List<DayEntry>,
        val activityEntries: List<ActivityEntry>,
    )

    private data class DayAggregate(
        val calories: Float,
        val steps: Int,
        val distanceKm: Double,
    )

    suspend fun importActivity(client: HealthConnectClient): ImportResult {
        val end = Instant.now()
        val start = end.minus(HealthConnectNutritionSync.DAYS_BACK, ChronoUnit.DAYS)
        val range = TimeRangeFilter.between(start, end)
        val zone = ZoneId.systemDefault()

        val calorieRecords = client.readAllPaged(TotalCaloriesBurnedRecord::class, range)
        val stepRecords = client.readAllPaged(StepsRecord::class, range)
        val distanceRecords = client.readAllPaged(DistanceRecord::class, range)
        val sessions = client.readAllPaged(ExerciseSessionRecord::class, range)

        Log.i(
            TAG,
            "read ${calorieRecords.size} calorie, ${stepRecords.size} step, " +
                "${distanceRecords.size} distance, ${sessions.size} session record(s)"
        )
        Log.i(
            TAG,
            "origins calories=${originCounts(calorieRecords)} steps=${originCounts(stepRecords)} " +
                "distance=${originCounts(distanceRecords)} sessions=${originCounts(sessions)}"
        )
        Log.i(
            TAG,
            "recording methods steps=${recordingMethodCounts(stepRecords)} " +
                "distance=${recordingMethodCounts(distanceRecords)} sessions=${recordingMethodCounts(sessions)}"
        )
        if (sessions.isEmpty()) {
            Log.w(
                TAG,
                "No ExerciseSessionRecord rows found. Activity Journal imports only real " +
                    "exercise/workout sessions; raw daily steps, distance, and calories are not " +
                    "converted into activity rows."
            )
        } else {
            sessions.take(10).forEach { s ->
                Log.i(
                    TAG,
                    "session sample id=${s.metadata.id} origin=${s.metadata.dataOrigin.packageName} " +
                        "type=${s.exerciseType} start=${s.startTime} end=${s.endTime} " +
                        "recordingMethod=${s.metadata.recordingMethod} title=${s.title}"
                )
            }
        }

        val calByDay = mutableMapOf<Long, Float>()
        val stepsByDay = mutableMapOf<Long, Int>()
        val distByDay = mutableMapOf<Long, Double>()
        val durationByDay = mutableMapOf<Long, Long>()

        for (r in calorieRecords) {
            val key = startOfDayMillis(r.startTime, zone)
            calByDay[key] = (calByDay[key] ?: 0f) + (r.energy?.inKilocalories?.toFloat() ?: 0f)
        }
        for (r in stepRecords) {
            val key = startOfDayMillis(r.startTime, zone)
            val c = r.count
            stepsByDay[key] = (stepsByDay[key] ?: 0) + c.toInt().coerceAtLeast(0)
        }
        for (r in distanceRecords) {
            val key = startOfDayMillis(r.startTime, zone)
            val km = (r.distance?.inMeters ?: 0.0) / 1000.0
            distByDay[key] = (distByDay[key] ?: 0.0) + km
        }
        for (s in sessions) {
            val key = startOfDayMillis(s.startTime, zone)
            val dur = Duration.between(s.startTime, s.endTime).toMillis().coerceAtLeast(0)
            durationByDay[key] = (durationByDay[key] ?: 0L) + dur
        }

        val dayKeys = calByDay.keys + stepsByDay.keys + distByDay.keys + durationByDay.keys
        val dayEntries = dayKeys.map { k ->
            val aggregate = client.aggregateDay(k, zone)
            val rawSteps = stepsByDay[k] ?: 0
            val rawDistance = distByDay[k] ?: 0.0
            val rawCalories = calByDay[k] ?: 0f
            if (
                rawSteps != aggregate.steps ||
                rawDistance != aggregate.distanceKm ||
                rawCalories != aggregate.calories
            ) {
                Log.i(
                    TAG,
                    "day aggregate ${Instant.ofEpochMilli(k).atZone(zone).toLocalDate()} " +
                        "raw steps=$rawSteps distanceKm=$rawDistance calories=$rawCalories -> " +
                        "deduped steps=${aggregate.steps} distanceKm=${aggregate.distanceKm} " +
                        "calories=${aggregate.calories}"
                )
            }
            DayEntry(
                startTime = k,
                totalCalories = aggregate.calories,
                totalDuration = durationByDay[k] ?: 0L,
                totalDistance = aggregate.distanceKm,
                totalSteps = aggregate.steps,
            )
        }.sortedBy { it.startTime }

        val activityEntries = buildList {
            for (s in sessions) {
                val type = mapExerciseType(s.exerciseType)
                if (type == ActivityType.Unknown || type == ActivityType.Still) continue
                val cal = caloriesOverlapping(calorieRecords, s.startTime, s.endTime)
                val dur = Duration.between(s.startTime, s.endTime).toMillis().coerceAtLeast(0)
                add(
                    ActivityEntry(
                        activity = type,
                        calories = cal,
                        startTime = s.startTime.toEpochMilli(),
                        duration = dur,
                        distance = null,
                        steps = null,
                        hcId = s.metadata.id,
                    )
                )
            }
        }

        Log.i(TAG, "import ${dayEntries.size} day aggregates, ${activityEntries.size} sessions")
        return ImportResult(dayEntries, activityEntries)
    }

    suspend fun mapSingleSession(
        client: HealthConnectClient,
        s: ExerciseSessionRecord
    ): ActivityEntry? {
        val type = mapExerciseType(s.exerciseType)
        if (type == ActivityType.Unknown || type == ActivityType.Still) return null
        val range = TimeRangeFilter.between(s.startTime, s.endTime)
        val calorieRecords = client.readAllPaged(TotalCaloriesBurnedRecord::class, range)
        val cal = caloriesOverlapping(calorieRecords, s.startTime, s.endTime)
        val dur = Duration.between(s.startTime, s.endTime).toMillis().coerceAtLeast(0)
        return ActivityEntry(
            activity = type,
            calories = cal,
            startTime = s.startTime.toEpochMilli(),
            duration = dur,
            distance = null,
            steps = null,
            hcId = s.metadata.id,
        )
    }

    private fun caloriesOverlapping(
        calorieRecords: List<TotalCaloriesBurnedRecord>,
        sessionStart: Instant,
        sessionEnd: Instant,
    ): Int {
        if (!sessionEnd.isAfter(sessionStart)) return 0
        return calorieRecords.sumOf { r ->
            if (intervalsOverlap(r.startTime, r.endTime, sessionStart, sessionEnd)) {
                (r.energy?.inKilocalories ?: 0.0).roundToInt().coerceAtLeast(0)
            } else 0
        }
    }

    private fun intervalsOverlap(
        aStart: Instant,
        aEnd: Instant,
        bStart: Instant,
        bEnd: Instant,
    ): Boolean = aStart.isBefore(bEnd) && aEnd.isAfter(bStart)

    private fun startOfDayMillis(instant: Instant, zone: ZoneId): Long =
        instant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    private suspend fun HealthConnectClient.aggregateDay(
        startOfDayMillis: Long,
        zone: ZoneId,
    ): DayAggregate {
        val start = Instant.ofEpochMilli(startOfDayMillis)
        val end = start.atZone(zone).plusDays(1).toInstant()
        val result = aggregate(
            AggregateRequest(
                metrics = setOf(
                    TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                    StepsRecord.COUNT_TOTAL,
                    DistanceRecord.DISTANCE_TOTAL,
                ),
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        return DayAggregate(
            calories = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                ?.inKilocalories
                ?.toFloat()
                ?: 0f,
            steps = (result[StepsRecord.COUNT_TOTAL] ?: 0L).toInt().coerceAtLeast(0),
            distanceKm = ((result[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0) / 1000.0)
                .coerceAtLeast(0.0),
        )
    }

    private fun <T : Record> originCounts(records: List<T>): Map<String, Int> =
        records.groupingBy { it.metadata.dataOrigin.packageName }.eachCount()

    private fun <T : Record> recordingMethodCounts(records: List<T>): Map<Int, Int> =
        records.groupingBy { it.metadata.recordingMethod }.eachCount()

    private suspend fun <T : Record> HealthConnectClient.readAllPaged(
        type: KClass<T>,
        filter: TimeRangeFilter,
    ): List<T> {
        val out = mutableListOf<T>()
        var pageToken: String? = null
        do {
            val response = readRecords(
                ReadRecordsRequest(
                    recordType = type,
                    timeRangeFilter = filter,
                    dataOriginFilter = emptySet(),
                    ascendingOrder = true,
                    pageSize = PAGE_SIZE,
                    pageToken = pageToken,
                )
            )
            @Suppress("UNCHECKED_CAST")
            out.addAll(response.records as List<T>)
            pageToken = response.pageToken.takeUnless { it.isNullOrBlank() }
        } while (pageToken != null)
        return out
    }

    private fun mapExerciseType(type: Int): ActivityType = when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> ActivityType.Other
        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> ActivityType.Badminton
        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> ActivityType.BaseBall
        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> ActivityType.BasketBall
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> ActivityType.Biking
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> ActivityType.BikingStationary
        ExerciseSessionRecord.EXERCISE_TYPE_BOOT_CAMP -> ActivityType.CrossFit
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING -> ActivityType.Boxing
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> ActivityType.Calisthenics
        ExerciseSessionRecord.EXERCISE_TYPE_CRICKET -> ActivityType.Cricket
        ExerciseSessionRecord.EXERCISE_TYPE_DANCING -> ActivityType.Dancing
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> ActivityType.Elliptical
        ExerciseSessionRecord.EXERCISE_TYPE_EXERCISE_CLASS -> ActivityType.Aerobics
        ExerciseSessionRecord.EXERCISE_TYPE_FENCING -> ActivityType.Fencing
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AMERICAN -> ActivityType.Football
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AUSTRALIAN -> ActivityType.Football
        ExerciseSessionRecord.EXERCISE_TYPE_FRISBEE_DISC -> ActivityType.Frisbee
        ExerciseSessionRecord.EXERCISE_TYPE_GOLF -> ActivityType.Golf
        ExerciseSessionRecord.EXERCISE_TYPE_GUIDED_BREATHING -> ActivityType.GuidedBreathing
        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> ActivityType.Gymnastics
        ExerciseSessionRecord.EXERCISE_TYPE_HANDBALL -> ActivityType.HandBall
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING ->
            ActivityType.HighIntensityIntervalTraining
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> ActivityType.Hiking
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_HOCKEY -> ActivityType.Hockey
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_SKATING -> ActivityType.IceSkating
        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS -> ActivityType.MartialArts
        ExerciseSessionRecord.EXERCISE_TYPE_PADDLING -> ActivityType.Kayaking
        ExerciseSessionRecord.EXERCISE_TYPE_PARAGLIDING -> ActivityType.ParaGliding
        ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> ActivityType.Pilates
        ExerciseSessionRecord.EXERCISE_TYPE_RACQUETBALL -> ActivityType.RacquetBall
        ExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING -> ActivityType.RockClimbing
        ExerciseSessionRecord.EXERCISE_TYPE_ROLLER_HOCKEY -> ActivityType.Hockey
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING -> ActivityType.Rowing
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> ActivityType.RowingMachine
        ExerciseSessionRecord.EXERCISE_TYPE_RUGBY -> ActivityType.Rugby
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> ActivityType.Running
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> ActivityType.RunningTreadMill
        ExerciseSessionRecord.EXERCISE_TYPE_SAILING -> ActivityType.Sailing
        ExerciseSessionRecord.EXERCISE_TYPE_SCUBA_DIVING -> ActivityType.ScubaDiving
        ExerciseSessionRecord.EXERCISE_TYPE_SKATING -> ActivityType.Skating
        ExerciseSessionRecord.EXERCISE_TYPE_SKIING -> ActivityType.Skiing
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWBOARDING -> ActivityType.SnowBoarding
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWSHOEING -> ActivityType.SnowShoeing
        ExerciseSessionRecord.EXERCISE_TYPE_SOCCER -> ActivityType.Football
        ExerciseSessionRecord.EXERCISE_TYPE_SOFTBALL -> ActivityType.SoftBall
        ExerciseSessionRecord.EXERCISE_TYPE_SQUASH -> ActivityType.Squash
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING,
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING_MACHINE,
        -> ActivityType.StairClimbing
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> ActivityType.StrengthTraining
        ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING -> ActivityType.Pilates
        ExerciseSessionRecord.EXERCISE_TYPE_SURFING -> ActivityType.Surfing
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
        -> ActivityType.Swimming
        ExerciseSessionRecord.EXERCISE_TYPE_TABLE_TENNIS -> ActivityType.TableTennis
        ExerciseSessionRecord.EXERCISE_TYPE_TENNIS -> ActivityType.Tennis
        ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL -> ActivityType.VolleyBall
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> ActivityType.Walking
        ExerciseSessionRecord.EXERCISE_TYPE_WATER_POLO -> ActivityType.WaterPolo
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> ActivityType.Weightlifting
        ExerciseSessionRecord.EXERCISE_TYPE_WHEELCHAIR -> ActivityType.WheelChair
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> ActivityType.Yoga
        else -> ActivityType.Other
    }

    private const val PAGE_SIZE = 1_000
}
