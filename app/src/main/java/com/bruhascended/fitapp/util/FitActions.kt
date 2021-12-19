package com.bruhascended.fitapp.util

import android.content.Context
import android.util.Log
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.activity.types.ActivityType
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.ui.addworkout.ActivitiesMap
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
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

fun dumpDayEntryBuckets(buckets: List<Bucket>): MutableList<DayEntry> {
    val periodicEntriesList = mutableListOf<DayEntry>()
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
        val entry = DayEntry(
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

fun insertActivityEntriesToDb(
    list: MutableList<ActivityEntry>,
    repository: ActivityEntryRepository
) {
    for (entry in list) {
        if (entry.activity != ActivityType.Unknown && entry.activity != ActivityType.Still) {
            val activityEntry: ActivityEntry? = findActivity(entry, repository)
            if (activityEntry != null) {
                entry.id = activityEntry.id
            }
            repository.writeEntry(entry)
        }
    }
}

fun insertPeriodicEntriesToDb(
    periodicEntriesList: MutableList<DayEntry>,
    repository: ActivityEntryRepository
) {
    for (entry in periodicEntriesList) {
        val periodicEntry: DayEntry? = findDayEntry(entry, repository)
        if (periodicEntry != null) {
            entry.startTime = periodicEntry.startTime
        }
        repository.insertDayEntry(entry)
    }
}

fun findActivity(entry: ActivityEntry, repository: ActivityEntryRepository): ActivityEntry? {
    val activityEntry: ActivityEntry? = repository.findByStartTime(entry.startTime)
    return activityEntry
}

fun findDayEntry(entry: DayEntry, repository: ActivityEntryRepository): DayEntry? {
    val periodicEntry: DayEntry? = repository.findDayEntryByStartTime(entry.startTime)
    return periodicEntry
}

fun signOut(context: Context) {
    Fitness.getConfigClient(context, getGoogleAccount(context, FitBuilder.fitnessOptions))
        .disableFit()
        .addOnSuccessListener {
            Log.d("eyo", "Disabled Google Fit")
        }
        .addOnFailureListener { e ->
            Log.d("eyo", "There was an error disabling Google Fit", e)
        }
    val signInOptions =
        GoogleSignInOptions.Builder().addExtension(FitBuilder.fitnessOptions).build()
    val client =
        GoogleSignIn.getClient(context, signInOptions)
    client.revokeAccess()
}