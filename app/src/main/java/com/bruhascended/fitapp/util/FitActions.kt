package com.bruhascended.fitapp.util

import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.PeriodicEntry
import com.bruhascended.fitapp.ui.addworkout.ActivitiesMap
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object FitBuilder {
    val estimatedStepSource = DataSource.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setType(DataSource.TYPE_DERIVED)
        .setStreamName("estimated_steps")
        .setAppPackageName("com.google.android.gms")
        .build()

    val fitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()
    }
}

fun dumpPeriodicEntryBuckets(buckets: List<Bucket>): MutableList<PeriodicEntry> {
    val periodicEntriesList = mutableListOf<PeriodicEntry>()
    for (bucket in buckets) {
        var calories: Float = 0f
        var steps: Int = 0
        var distance: Double = .0
        var duration: Long = 0L
        for (dataSet in bucket.dataSets) {
            when {
                dataSet.dataType == DataType.TYPE_MOVE_MINUTES && dataSet.dataPoints.isNotEmpty() -> {
                    duration =
                        (dataSet.dataPoints[0].getValue(Field.FIELD_DURATION)
                            .asInt() * 60000).toLong()
                }

                dataSet.dataType == DataType.TYPE_STEP_COUNT_DELTA && dataSet.dataPoints.isNotEmpty() -> {
                    steps =
                        dataSet.dataPoints[0].getValue(Field.FIELD_STEPS)
                            .asInt()
                }
                dataSet.dataType == DataType.TYPE_DISTANCE_DELTA && dataSet.dataPoints.isNotEmpty() -> {
                    distance =
                        dataSet.dataPoints[0].getValue(Field.FIELD_DISTANCE)
                            .asFloat()
                            .toDouble() / 1000
                }
                dataSet.dataType == DataType.TYPE_CALORIES_EXPENDED && dataSet.dataPoints.isNotEmpty() -> {
                    calories =
                        dataSet.dataPoints[0].getValue(Field.FIELD_CALORIES)
                            .asFloat()

                }
            }
        }
        val entry = PeriodicEntry(
            bucket.getStartTime(TimeUnit.MILLISECONDS),
            calories,
            duration,
            distance,
            steps
        )
        periodicEntriesList.add(entry)
    }
    return periodicEntriesList
}

fun dumpActivityEntryBuckets(buckets: List<Bucket>): MutableList<ActivityEntry> {
    val activitiesEntryList = mutableListOf<ActivityEntry>()
    for (bucket in buckets) {
        var calories: Int? = null
        var steps: Int? = null
        var distance: Double? = null
        for (dataSet in bucket.dataSets) {
            when {
                dataSet.dataType == DataType.TYPE_STEP_COUNT_DELTA && dataSet.dataPoints.isNotEmpty() -> {
                    steps =
                        dataSet.dataPoints[0].getValue(Field.FIELD_STEPS)
                            .asInt()
                }
                dataSet.dataType == DataType.TYPE_DISTANCE_DELTA && dataSet.dataPoints.isNotEmpty() -> {
                    distance =
                        dataSet.dataPoints[0].getValue(Field.FIELD_DISTANCE)
                            .asFloat()
                            .toDouble() / 1000
                }
                dataSet.dataType == DataType.TYPE_CALORIES_EXPENDED && dataSet.dataPoints.isNotEmpty() -> {
                    calories =
                        dataSet.dataPoints[0].getValue(Field.FIELD_CALORIES)
                            .asFloat()
                            .roundToInt()
                }
            }
        }
        val entry = calories?.let { it1 ->
            ActivitiesMap.getActivityType(bucket.activity)?.let { it2 ->
                ActivityEntry(
                    it2,
                    it1,
                    bucket.getStartTime(TimeUnit.MILLISECONDS),
                    bucket.getEndTime(TimeUnit.MILLISECONDS) - bucket.getStartTime(
                        TimeUnit.MILLISECONDS
                    ),
                    distance,
                    steps
                )
            }
        }
        if (entry != null) {
            activitiesEntryList.add(entry)
        }
    }
    return activitiesEntryList
}