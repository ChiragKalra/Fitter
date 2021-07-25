package com.bruhascended.fitapp.ui.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.PeriodicEntry
import com.bruhascended.db.activity.types.ActivityType
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.ui.addworkout.ActivitiesMap
import com.bruhascended.fitapp.util.DateTimePresenter
import com.bruhascended.fitapp.util.getTodayMidnightTime
import com.bruhascended.fitapp.util.getTodayStartTime
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

enum class DAYS(val days: Int) {
    WEEK(7),
    MONTH(30),
    YEAR(365)
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val app = application
    private val repository by ActivityEntryRepository.Delegate(app)
    val cal = Calendar.getInstance(TimeZone.getDefault())

    var endTime = cal.timeInMillis
    var startTime = cal.getTodayStartTime(cal)
    val estimatedStepSource = DataSource.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setType(DataSource.TYPE_DERIVED)
        .setStreamName("estimated_steps")
        .setAppPackageName("com.google.android.gms")
        .build()

    fun syncPassiveData(context: Context, googleAccount: GoogleSignInAccount) {
        cal.add(Calendar.DAY_OF_YEAR, -DAYS.WEEK.days)
        startTime = cal.timeInMillis
        Log.d("eyo","${DateTimePresenter(context,startTime).fullTimeAndDate}")
        Log.d("eyo","${DateTimePresenter(context,endTime).fullTimeAndDate}")
        val periodicEntriesList = mutableListOf<PeriodicEntry>()

        val redRequest = DataReadRequest.Builder()
            .bucketByTime(30, TimeUnit.MINUTES)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .aggregate(estimatedStepSource)
            .aggregate(DataType.TYPE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_MOVE_MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .enableServerQueries()
            .build()

        Fitness.getHistoryClient(context, googleAccount)
            .readData(redRequest)
            .addOnSuccessListener {
                for (bucket in it.buckets) {
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
                Log.d("eyo","reahed insrrtperdiodicenttries fn")
                insertPeriodicEntriesToDb(periodicEntriesList)
            }
            .addOnFailureListener {
                Log.d("eyo", it.message.toString())
            }
    }

    fun syncActivities(context: Context, googleAccount: GoogleSignInAccount) {
        cal.timeInMillis = Date().time
        endTime = cal.timeInMillis
        startTime = cal.getTodayStartTime(cal)

        val entriesList = mutableListOf<ActivityEntry>()
        val numOfDays = DAYS.MONTH.days
        var tempNumOfDays = 0

        CoroutineScope(IO).launch {
            for (day in 1..numOfDays) {

                val readRequest = DataReadRequest.Builder()
                    .bucketByActivitySegment(5, TimeUnit.MINUTES)
                    .aggregate(estimatedStepSource)
                    .aggregate(DataType.TYPE_DISTANCE_DELTA)
                    .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                    .enableServerQueries()
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()


                Fitness.getHistoryClient(context, googleAccount)
                    .readData(readRequest)
                    .addOnSuccessListener {
                        tempNumOfDays += 1
                        for (bucket in it.buckets) {
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
                                entriesList.add(entry)
                            }
                        }
                        if (tempNumOfDays == numOfDays)
                            insertEntriesToDb(entriesList)
                    }
                    .addOnFailureListener {
                        Log.d("eyo", "${it.message}")
                    }
                cal.add(Calendar.DAY_OF_WEEK, -1)
                endTime = cal.getTodayMidnightTime(cal)
                startTime = cal.getTodayStartTime(cal)
            }
        }
    }

    private fun insertPeriodicEntriesToDb(periodicEntriesList: MutableList<PeriodicEntry>) {
        Log.d("eyo","reahed inside insrrtperdiodicenttries fn")
        CoroutineScope(IO).launch {
            for (entry in periodicEntriesList) {
                val periodicEntry: PeriodicEntry? = findPeriodicEntry(entry)
                if (periodicEntry != null) {
                    entry.startTime = periodicEntry.startTime
                }
                repository.insertPeriodicEntry(entry)
            }
        }
    }

    private fun insertEntriesToDb(list: MutableList<ActivityEntry>) {
        CoroutineScope(IO).launch {
            for (entry in list) {
                if (entry.activity != ActivityType.Unknown && entry.activity != ActivityType.Still) {
                    val activityEntry: ActivityEntry? = findActivity(entry)
                    if (activityEntry != null) {
                        entry.id = activityEntry.id
                    }
                    repository.writeEntry(entry)
                }
            }
        }
    }

    private fun findActivity(entry: ActivityEntry): ActivityEntry? {
        val activityEntry: ActivityEntry? = repository.findByStartTime(entry.startTime)
        return activityEntry
    }

    private fun findPeriodicEntry(entry: PeriodicEntry): PeriodicEntry? {
        val periodicEntry: PeriodicEntry? = repository.findPeriodicEntryByStartTime(entry.startTime)
        return periodicEntry
    }

}
