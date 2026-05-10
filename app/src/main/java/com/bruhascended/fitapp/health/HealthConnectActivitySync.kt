package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
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

object HealthConnectActivitySync {

    private const val TAG = "HealthConnectActivity"

    data class ImportResult(
        val dayEntries: List<DayEntry>,
        val activityEntries: List<ActivityEntry>,
    )

    suspend fun importActivity(client: HealthConnectClient): ImportResult {
        val end = Instant.now()
        val start = end.minus(HealthConnectNutritionSync.DAYS_BACK, ChronoUnit.DAYS)
        val range = TimeRangeFilter.between(start, end)
        val zone = ZoneId.systemDefault()

        val calorieRecords = client.readRecords(
            ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRangeFilter = range)
        ).records
        val stepRecords = client.readRecords(
            ReadRecordsRequest(StepsRecord::class, timeRangeFilter = range)
        ).records
        val distanceRecords = client.readRecords(
            ReadRecordsRequest(DistanceRecord::class, timeRangeFilter = range)
        ).records
        val sessions = client.readRecords(
            ReadRecordsRequest(ExerciseSessionRecord::class, timeRangeFilter = range)
        ).records

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
            DayEntry(
                startTime = k,
                totalCalories = calByDay[k] ?: 0f,
                totalDuration = durationByDay[k] ?: 0L,
                totalDistance = distByDay[k] ?: 0.0,
                totalSteps = stepsByDay[k] ?: 0,
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
        val calorieRecords = client.readRecords(
            ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRangeFilter = range)
        ).records
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
}
