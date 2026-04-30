package com.bruhascended.db

import android.content.Context
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.bruhascended.db.friends.FriendDatabase
import com.bruhascended.db.friends.FriendDatabaseFactory
import com.bruhascended.db.friends.entities.Friend
import com.bruhascended.db.friends.entities.MonthlyStats
import com.bruhascended.db.friends.entities.WeeklyStats
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class FriendsDbTest {
    private lateinit var db: FriendDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = FriendDatabaseFactory(context).allowMainThreadOperations(false).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun friendDbTest() {
        val friend = Friend(
            "hello123@domain.com",
            "username",
            WeeklyStats(),
            MonthlyStats()
        )

        // insertion test
        val id = db.friendManager().insert(friend)
        val byId = db.friendManager().findById(friend.uid)
        assertEquals(friend, byId)

        // deletion test
        db.friendManager().delete(friend)
        val afterDelete = db.friendManager().findById(id)
        assertEquals(afterDelete, null)
    }

}
