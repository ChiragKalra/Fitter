package com.bruhascended.fitapp

import android.app.Application
import android.util.Log
import androidx.work.*
import com.bruhascended.fitapp.workers.PeriodicEntryWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setUpRecurringWork()
    }

    private fun setUpRecurringWork() {
        CoroutineScope(IO).launch {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<PeriodicEntryWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            Log.d("eyo", "Work request scheduled")
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                PeriodicEntryWorker.name,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}