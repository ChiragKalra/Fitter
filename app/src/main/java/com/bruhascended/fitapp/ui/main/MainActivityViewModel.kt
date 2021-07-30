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
import com.bruhascended.fitapp.util.*
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

class MainActivityViewModel(val app: Application) : AndroidViewModel(app) {
    private val repository by ActivityEntryRepository.Delegate(app)
    val cal = Calendar.getInstance(TimeZone.getDefault())

    var endTime = cal.timeInMillis
    var startTime = cal.getTodayStartTime(cal)
    val estimatedStepSource = FitBuilder.estimatedStepSource

    fun syncPeriodicData(context: Context, googleAccount: GoogleSignInAccount) {
        cal.add(Calendar.DAY_OF_WEEK,-6)
        startTime = cal.timeInMillis

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
                CoroutineScope(IO).launch { insertPeriodicEntriesToDb(dumpPeriodicEntryBuckets(it.buckets)) }
            }
            .addOnFailureListener {
                Log.d("eyo", it.message.toString())
            }
    }

    fun syncActivities(context: Context, googleAccount: GoogleSignInAccount) {
        cal.timeInMillis = Date().time
        endTime = cal.timeInMillis
        startTime = cal.getTodayStartTime(cal)

        CoroutineScope(IO).launch {
            for (day in 1..7) {

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
                        insertEntriesToDb(dumpActivityEntryBuckets(it.buckets))
                    }
                    .addOnFailureListener {
                        Log.d("eyo", "${it.message}")
                    }

                cal.add(Calendar.DAY_OF_WEEK,-1)
                startTime = cal.getTodayStartTime(cal)
                endTime = cal.getTodayMidnightTime(cal)
            }
        }
    }

    private fun insertPeriodicEntriesToDb(periodicEntriesList: MutableList<PeriodicEntry>) {
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
