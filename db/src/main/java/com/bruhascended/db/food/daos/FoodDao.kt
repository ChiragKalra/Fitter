package com.bruhascended.db.food.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.food.entities.Food

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (food: Food)

    @Delete
    fun delete (food: Food)

    @Query("SELECT * FROM food WHERE foodName LIKE :foodName")
    fun findByName (foodName: String): Food?

    @Query("SELECT * FROM food WHERE foodName LIKE :foodName")
    fun getLive (foodName: String): LiveData<Food?>

    @Query("SELECT * FROM food ORDER BY foodName ASC")
    fun loadAllPaged(): PagingSource<Int, Food>

    @Query("SELECT * FROM food")
    fun loadAllSync(): List<Food>

    @Query("SELECT COUNT(foodName) FROM food")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM food WHERE LOWER(foodName) LIKE :key OR LOWER(foodName) LIKE :altKey ORDER BY foodName ASC")
    fun searchPaged (key: String, altKey: String = "% $key"): PagingSource<Int, Food>

    @Query("SELECT * FROM food WHERE LOWER(foodName) LIKE :key OR LOWER(foodName) LIKE :altKey ORDER BY foodName ASC")
    fun searchLive (key: String, altKey: String = "% $key"): LiveData<MutableList<Food>>

    @Query("SELECT * FROM food WHERE LOWER(foodName) LIKE :key OR LOWER(foodName) LIKE :altKey ORDER BY foodName ASC")
    fun searchSync (key: String, altKey: String = "% $key"): List<Food>
}