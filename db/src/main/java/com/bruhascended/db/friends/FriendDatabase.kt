package com.bruhascended.db.friends

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bruhascended.db.friends.doas.FriendDao
import com.bruhascended.db.friends.entities.Friend

@Database(
    entities = [
        Friend::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class FriendDatabase : RoomDatabase() {
    abstract fun friendManager(): FriendDao
}
