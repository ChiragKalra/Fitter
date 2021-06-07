package com.bruhascended.db.food.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.food.entities.Entry

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (entry: Entry): Long

    @Delete
    fun delete (entry: Entry)

    @Query("SELECT * FROM entry WHERE entryId LIKE :entryId")
    fun findById (entryId: String): Entry?

    @Query("SELECT * FROM entry WHERE entryId LIKE :entryId")
    fun getLive (entryId: String): LiveData<Entry?>

    @Query("SELECT * FROM entry ORDER BY entryId ASC")
    fun loadAllPaged(): PagingSource<Int, Entry>

    @Query("SELECT * FROM entry")
    fun loadAllSync(): List<Entry>

    @Query("SELECT COUNT(entryId) FROM entry")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM entry WHERE LOWER(entryId) LIKE :key OR LOWER(entryId) LIKE :altKey ORDER BY entryId ASC")
    fun searchPaged (key: String, altKey: String = "% $key"): PagingSource<Int, Entry>

    @Query("SELECT * FROM entry WHERE LOWER(entryId) LIKE :key OR LOWER(entryId) LIKE :altKey ORDER BY entryId ASC")
    fun searchLive (key: String, altKey: String = "% $key"): LiveData<List<Entry>>

    @Query("SELECT * FROM entry WHERE LOWER(entryId) LIKE :key OR LOWER(entryId) LIKE :altKey ORDER BY entryId ASC")
    fun searchSync (key: String, altKey: String = "% $key"): List<Entry>
}