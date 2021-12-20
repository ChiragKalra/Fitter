package com.bruhascended.db.friends.doas

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.friends.entities.Friend

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (friend: Friend): Long

    @Delete
    fun delete (friend: Friend)

    @Query("SELECT * FROM friends WHERE uid LIKE :id")
    fun findById (id: Long): Friend?

    @Query("SELECT * FROM friends WHERE uid LIKE :id")
    fun loadLive (id: Long): LiveData<Friend?>

    @Query("SELECT * FROM friends")
    fun loadAllPaged(): PagingSource<Int, Friend>

    @Query("SELECT * FROM friends")
    fun loadAllSync(): List<Friend>

    @Query("SELECT * FROM friends")
    fun loadAllLive(): LiveData<List<Friend>>

    @Query("SELECT COUNT(uid) FROM friends")
    fun getLiveCount(): LiveData<Int>
}