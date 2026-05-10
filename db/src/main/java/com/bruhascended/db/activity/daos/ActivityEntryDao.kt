package com.bruhascended.db.activity.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.activity.entities.ActivityEntry

@Dao
interface ActivityEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (activity_entry: ActivityEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<ActivityEntry>): List<Long>

    @Delete
    fun delete (activity_entry: ActivityEntry)

    @Query("SELECT * FROM activity_entry WHERE id LIKE :id")
    fun findById (id: Long): ActivityEntry?

    @Query("SELECT * FROM activity_entry WHERE startTime LIKE :timeInMillis")
    fun findByStartTime (timeInMillis: Long): ActivityEntry?

    @Query("SELECT * FROM activity_entry WHERE id LIKE :id")
    fun loadLive (id: Long): LiveData<ActivityEntry?>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllPaged(): PagingSource<Int, ActivityEntry>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllSync(): List<ActivityEntry>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllLive(): LiveData<List<ActivityEntry>>

    @Query("DELETE FROM activity_entry")
    fun deleteAll()

    @Query("SELECT COUNT(id) FROM activity_entry")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM activity_entry WHERE hcId LIKE :hcId LIMIT 1")
    fun findByHcId(hcId: String): ActivityEntry?

    @Query("DELETE FROM activity_entry WHERE hcId LIKE :hcId")
    fun deleteByHcId(hcId: String)
}