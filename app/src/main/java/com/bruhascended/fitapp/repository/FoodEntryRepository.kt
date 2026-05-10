package com.bruhascended.fitapp.repository

import android.app.Application
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.reflect.KProperty
import androidx.health.connect.client.HealthConnectClient
import com.bruhascended.fitapp.health.HealthConnectNutritionSync

class FoodEntryRepository(
    mApp: Application
) {

    companion object {
        private var repository: FoodEntryRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FoodEntryRepository {
            if (repository == null) {
                repository = FoodEntryRepository(app)
            }
            return repository!!
        }
    }

    private val db = FoodEntryDatabaseFactory(mApp).build()

    fun searchConsumedFood(query: String): LiveData<List<Food>> {
        return db.foodManager().searchLive(query)
    }

    suspend fun writeEntry(foodEntry: FoodEntry) = db.insertEntry(foodEntry)

    suspend fun writeEntry(food: Food, entry: Entry) = db.insertEntry(food, entry)

    fun deleteEntry(foodEntry: FoodEntry) = db.deleteEntry(foodEntry)

    fun loadConsumedFoodEntries(): Flow<PagingData<FoodEntry>> {
        return Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 60,
                maxSize = 180,
            )
        ) {
            db.loadFoodEntry().allPaged()
        }.flow
    }

    fun loadLastWeek(): LiveData<List<DayEntry>> {
        val date = Calendar.getInstance().apply {
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
                val newIdSet = HashSet<Long>().apply {
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
     * Replaces local food journal with [NutritionRecord] data from Health Connect.
     */
    suspend fun replaceJournalFromHealthConnect(client: HealthConnectClient) =
        withContext(Dispatchers.IO) {
            val pairs = HealthConnectNutritionSync.fetchNutritionPairs(client)
            db.clearAllFoodData()
            for ((food, entry) in pairs) {
                db.insertEntry(food, entry)
            }
        }

    suspend fun upsertFromHcRecord(food: Food, entry: Entry) = withContext(Dispatchers.IO) {
        val existingEntry = entry.hcId?.let { db.entryManager().findByHcId(it) }
        if (existingEntry != null) {
            val existingFoodEntry = db.loadFoodEntry().singleById(existingEntry.entryId!!)
            if (existingFoodEntry != null) {
                db.deleteEntry(existingFoodEntry)
            }
        }
        db.insertEntry(food, entry)
    }

    suspend fun deleteByHcId(hcId: String) = withContext(Dispatchers.IO) {
        val existingEntry = db.entryManager().findByHcId(hcId)
        if (existingEntry != null) {
            val existingFoodEntry = db.loadFoodEntry().singleById(existingEntry.entryId!!)
            if (existingFoodEntry != null) {
                db.deleteEntry(existingFoodEntry)
            }
        }
    }

    fun clearAllFoodData() {
        db.clearAllFoodData()
    }
}
