package com.bruhascended.db.activity.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.activity.entities.ActivityEntry

@Dao
interface ActivityEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (activity_entry: ActivityEntry): Long

    @Delete
    fun delete (activity_entry: ActivityEntry)

    @Query("SELECT * FROM activity_entry WHERE id LIKE :id")
    fun findById (id: Long): ActivityEntry?

    @Query("SELECT * FROM activity_entry WHERE id LIKE :id")
    fun loadLive (id: Long): LiveData<ActivityEntry?>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllPaged(): PagingSource<Int, ActivityEntry>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllSync(): List<ActivityEntry>

    @Query("SELECT * FROM activity_entry ORDER BY startTime DESC")
    fun loadAllLive(): LiveData<List<ActivityEntry>>

    @Query("SELECT COUNT(id) FROM activity_entry")
    fun getLiveCount(): LiveData<Int>
}