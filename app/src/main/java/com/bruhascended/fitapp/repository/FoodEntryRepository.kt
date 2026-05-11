package com.bruhascended.fitapp.repository

import android.app.Application
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.food.FoodEntryDatabaseFactory
import com.bruhascended.db.food.entities.DayEntry
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.health.HealthConnectNutritionExport
import com.bruhascended.fitapp.health.HealthConnectNutritionSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KProperty

class FoodEntryRepository(
    private val mApp: Application,
) {

    companion object {
        private const val TAG = "FoodEntryRepo"
        private var repository: FoodEntryRepository? = null
    }

    class Delegate(
        private val app: Application,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FoodEntryRepository {
            if (repository == null) {
                repository = FoodEntryRepository(app)
            }
            return repository!!
        }
    }

    private val db = FoodEntryDatabaseFactory(mApp).build()
    private val prefs = PreferencesRepository(mApp)

    /**
     * Dedupe must run off the main thread (Room forbids transactions on main unless explicitly
     * opted in on the factory — we do not opt in for the app singleton).
     */
    private val housekeepingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        housekeepingScope.launch {
            runCatching { db.dropDuplicateImportedHealthConnectRows() }
                .onFailure { Log.w(TAG, "HC duplicate cleanup failed: ${it.message}", it) }
        }
    }

    fun searchConsumedFood(query: String): LiveData<List<Food>> {
        return db.foodManager().searchLive(query)
    }

    suspend fun writeEntry(foodEntry: FoodEntry): Long =
        withContext(Dispatchers.IO) {
            val id = db.insertEntry(foodEntry)
            maybePushNutritionExport(id)
            id
        }

    suspend fun writeEntry(food: Food, entry: Entry): Long =
        withContext(Dispatchers.IO) {
            val id = db.insertEntry(food, entry)
            maybePushNutritionExport(id)
            id
        }

    /** Replace-edit must be sequential delete → insert so Health Connect purge + IDs stay coherent. */
    suspend fun replaceJournalEntry(previous: FoodEntry?, food: Food, entry: Entry): Long =
        withContext(Dispatchers.IO) {
            previous?.let { deleteEntryInternal(it) }
            val id = db.insertEntry(food, entry)
            maybePushNutritionExport(id)
            id
        }

    suspend fun deleteEntry(foodEntry: FoodEntry) =
        withContext(Dispatchers.IO) {
            deleteEntryInternal(foodEntry)
        }

    private suspend fun deleteEntryInternal(foodEntry: FoodEntry) {
        purgeClientExportBeforeLocalDeleteIfNeeded(foodEntry)
        db.deleteEntry(foodEntry)
    }

    suspend fun loadAllFoodEntriesSync(): List<FoodEntry> =
        withContext(Dispatchers.IO) {
            db.loadFoodEntry().allSync()
        }

    fun loadConsumedFoodEntries(): Flow<PagingData<FoodEntry>> {
        return Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 60,
                maxSize = 180,
            ),
        ) {
            db.loadFoodEntry().allPaged()
        }.flow
    }

    fun loadLastWeek(): LiveData<List<DayEntry>> {
        val date =
            Calendar.getInstance().apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time
        return db.getLiveWeekly(date)
    }

    fun loadCount(n: Int): LiveData<List<Food>> = db.foodManager().topNLive(n)

    fun loadLiveFoodEntries(): LiveData<List<FoodEntry>> = db.loadFoodEntry().allLive()

    fun loadLiveSeparator(date: Date): LiveData<DayEntry?> =
        db.getLiveDayEntry(date.time)

    fun loadLiveLastItem(): MutableLiveData<HashSet<Long>> {
        val liveLastIds = MutableLiveData<HashSet<Long>>()
        loadLiveFoodEntries().observeForever { all ->
            CoroutineScope(Dispatchers.IO).launch {
                val allArr = all.toTypedArray()
                val newIdSet =
                    HashSet<Long>().apply {
                        if (allArr.isNotEmpty()) {
                            add(allArr.last().entry.entryId!!)
                        }
                        if (allArr.size > 1) {
                            allArr.slice(0 until all.size - 1).forEachIndexed { ind, foodEntry ->
                                if (foodEntry.entry.date != allArr[ind + 1].entry.date) {
                                    add(foodEntry.entry.entryId!!)
                                }
                            }
                        }
                    }
                liveLastIds.postValue(newIdSet)
            }
        }
        return liveLastIds
    }

    /**
     * Replaces local food journal with [NutritionRecord] data from Health Connect (other apps).
     * Clears NutritionRecord rows previously written by [mApp]'s Health Connect exporter first so
     * destructive re-import doesn't leave orphaned Fitter meals in Health Connect.
     */
    suspend fun replaceJournalFromHealthConnect(client: HealthConnectClient) =
        withContext(Dispatchers.IO) {
            runCatching {
                HealthConnectNutritionExport.deleteNutritionRecordsAuthoredBy(client, mApp.packageName)
            }.onFailure { Log.w(TAG, "Pre-replace HC app-origin purge: ${it.message}") }

            val pairs = HealthConnectNutritionSync.fetchNutritionPairs(client)
            db.clearAllFoodData()
            for ((food, entry) in pairs) {
                db.insertEntry(food, entry)
            }
        }

    suspend fun upsertFromHcRecord(food: Food, entry: Entry) =
        withContext(Dispatchers.IO) {
            val existingEntry = entry.hcId?.let { db.entryManager().findByHcId(it) }
            if (existingEntry != null) {
                val existingFoodEntry = db.loadFoodEntry().singleById(existingEntry.entryId!!)
                if (existingFoodEntry != null) {
                    purgeClientExportBeforeLocalDeleteIfNeeded(existingFoodEntry)
                    db.deleteEntry(existingFoodEntry)
                }
            }
            db.insertEntry(food, entry)
        }

    suspend fun deleteByHcId(hcId: String) =
        withContext(Dispatchers.IO) {
            val existingEntry = db.entryManager().findByHcId(hcId)
            if (existingEntry != null) {
                val existingFoodEntry = db.loadFoodEntry().singleById(existingEntry.entryId!!)
                if (existingFoodEntry != null) {
                    purgeClientExportBeforeLocalDeleteIfNeeded(existingFoodEntry)
                    db.deleteEntry(existingFoodEntry)
                }
            }
        }

    fun clearAllFoodData() {
        db.clearAllFoodData()
    }

    private suspend fun purgeClientExportBeforeLocalDeleteIfNeeded(foodEntry: FoodEntry) {
        val row = foodEntry.entry
        if (row.hcId != null) return
        val entryId = row.entryId ?: return
        if (HealthConnectClient.getSdkStatus(mApp) != HealthConnectClient.SDK_AVAILABLE) return
        val client = HealthConnectClient.getOrCreate(mApp)
        if (!HealthConnectNutritionExport.hasWriteNutrition(client)) return
        HealthConnectNutritionExport.purgeClientNutritionRecords(client, prefs, entryId)
    }

    private suspend fun maybePushNutritionExport(newRowId: Long) {
        if (HealthConnectClient.getSdkStatus(mApp) != HealthConnectClient.SDK_AVAILABLE) return
        val client = HealthConnectClient.getOrCreate(mApp)
        if (!HealthConnectNutritionExport.hasWriteNutrition(client)) return
        val fe = db.loadFoodEntry().singleById(newRowId) ?: return
        runCatching {
            HealthConnectNutritionExport.upsertExportedFoodEntry(client, prefs, fe)
        }.onFailure { Log.w(TAG, "HC nutrition export skipped: ${it.message}") }
    }
}
