package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.fitapp.util.DateTimePresenter
import com.bruhascended.fitapp.util.getGoogleAccount
import com.bruhascended.fitapp.util.getTodayMidnightTime
import com.bruhascended.fitapp.util.getTodayStartTime
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import org.tensorflow.lite.schema.LogicalAndOptions
import java.util.*
import java.util.concurrent.TimeUnit

class PeriodicEntryWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        val fitnessOptions: FitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .build()
    }

    override suspend fun doWork(): Result {
        try {
            performSync(context)
            Log.d("eyo","Sync is run")
        } catch (e: Exception) {
            Log.d("eyo", "${e.message}")
            return Result.retry()
        }
        return Result.success()
    }
}

private fun performSync(context: Context) {
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.add(Calendar.DAY_OF_WEEK,-8)
    val endTime = cal.getTodayMidnightTime(cal)
    cal.add(Calendar.WEEK_OF_MONTH, -1)
    val startTime = cal.getTodayStartTime(cal)
    Log.d("eyo","${DateTimePresenter(context,startTime).fullTimeAndDate}")
    Log.d("eyo","${DateTimePresenter(context,endTime).fullTimeAndDate}")

    val estimatedStepSource = DataSource.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setType(DataSource.TYPE_DERIVED)
        .setStreamName("estimated_steps")
        .setAppPackageName("com.google.android.gms")
        .build()

    val readRequest = DataReadRequest.Builder()
        .bucketByTime(30, TimeUnit.MINUTES)
        .aggregate(DataType.TYPE_CALORIES_EXPENDED)
        .aggregate(estimatedStepSource)
        .aggregate(DataType.TYPE_DISTANCE_DELTA)
        .aggregate(DataType.TYPE_MOVE_MINUTES)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .enableServerQueries()
        .build()

    Fitness.getHistoryClient(context, getGoogleAccount(context, PeriodicEntryWorker.fitnessOptions))
        .readData(readRequest)
        .addOnSuccessListener {
            Log.d("eyo", "${it.buckets.size}")
        }
        .addOnFailureListener {
            Log.d("eyo", "${it.message}")
        }

}