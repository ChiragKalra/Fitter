package com.bruhascended.fitapp.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.food.FoodEntryDatabaseFactory
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KProperty

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

    data class SeparatorInfo (
        var totalCalories: Int = 0,
        val totalNutrients: EnumMap<NutrientType, Double> =
            EnumMap<NutrientType, Double>(NutrientType::class.java)
    ) {
        operator fun plus(other: SeparatorInfo) = SeparatorInfo(
            totalCalories + other.totalCalories,
        ).also {
            totalNutrients.forEach { (k, v) ->
                it.totalNutrients[k] = v + (it.totalNutrients[k] ?: .0)
            }
            other.totalNutrients.forEach { (k, v) ->
                it.totalNutrients[k] = v + (it.totalNutrients[k] ?: .0)
            }
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

    fun loadCount(n: Int): LiveData<List<Food>> = db.foodManager().topNLive(n)

    fun loadLiveFoodEntries(): LiveData<List<FoodEntry>> = db.loadFoodEntry().allLive()

    fun loadLiveSeparators(): MutableLiveData<HashMap<Date, SeparatorInfo>> {
        val liveInfoMap = MutableLiveData<HashMap<Date, SeparatorInfo>>()
        loadLiveFoodEntries().observeForever { all ->
            CoroutineScope(Dispatchers.IO).launch {
                val infoMap = HashMap<Date, SeparatorInfo>()
                all.forEach {
                    val date = it.entry.date
                    if (!infoMap.containsKey(date)) {
                        infoMap[date] = SeparatorInfo()
                    }
                    infoMap[date]?.apply {
                        totalCalories += it.entry.calories
                        val amountPerQuantity = it.food.weightInfo[it.entry.quantityType]
                        if (amountPerQuantity != null) {
                            val amount = it.entry.quantity * amountPerQuantity
                            it.food.nutrientInfo.forEach { (key, value) ->
                                totalNutrients[key] = (totalNutrients[key] ?: .0) + value * amount
                            }
                        }
                    }
                }
                liveInfoMap.postValue(infoMap)
            }
        }
        return liveInfoMap
    }

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
}
