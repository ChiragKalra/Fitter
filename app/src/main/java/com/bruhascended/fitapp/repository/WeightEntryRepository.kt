package com.bruhascended.fitapp.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.LiveData
import com.bruhascended.db.weight.WeightEntryDatabase
import com.bruhascended.db.weight.WeightEntryDatabaseFactory
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.health.HealthConnectWeightSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.reflect.KProperty

class WeightEntryRepository(
    context: Context,
) {

    companion object {
        private var repository: WeightEntryRepository? = null
    }

    class Delegate(
        private val context: Context,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): WeightEntryRepository {
            if (repository == null) {
                repository = WeightEntryRepository(context)
            }
            return repository!!
        }
    }

    private val db: WeightEntryDatabase = WeightEntryDatabaseFactory(context).build()

    suspend fun writeEntry(entry: WeightEntry): Long =
        withContext(Dispatchers.IO) {
            db.entryManager().insert(entry)
        }

    fun loadEntriesRangeLive(startDate: Date, endDate: Date): LiveData<List<WeightEntry>> =
        db.entryManager().getTimeRangeLive(startDate.time, endDate.time)

    fun loadEntriesRangeSync(startDate: Date, endDate: Date): List<WeightEntry> =
        db.entryManager().getTimeRangeSync(startDate.time, endDate.time)

    fun latestSync(): WeightEntry? = db.entryManager().latestSync()

    fun latestLive(): LiveData<WeightEntry?> = db.entryManager().latestLive()

    suspend fun replaceFromHealthConnect(client: HealthConnectClient) =
        withContext(Dispatchers.IO) {
            val entries = HealthConnectWeightSync.importWeights(client)
            db.clearAllWeightData()
            if (entries.isNotEmpty()) {
                db.entryManager().insertAll(entries)
            }
        }

    fun upsertFromHcRecord(entry: com.bruhascended.db.weight.entities.WeightEntry) {
        val existing = entry.hcId?.let { db.entryManager().findByHcId(it) }
        if (existing != null) {
            entry.id = existing.id
        }
        db.entryManager().insert(entry)
    }

    fun deleteByHcId(hcId: String) {
        db.entryManager().deleteByHcId(hcId)
    }

    fun clearAllWeightData() {
        db.clearAllWeightData()
    }
}
