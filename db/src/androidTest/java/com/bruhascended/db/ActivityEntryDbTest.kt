package com.bruhascended.db

import android.content.Context
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.bruhascended.db.activity.ActivityEntryDatabase
import com.bruhascended.db.activity.ActivityEntryDatabaseFactory
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.types.ActivityType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class ActivityEntryDbTest {
    private lateinit var db: ActivityEntryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = ActivityEntryDatabaseFactory(context).allowMainThreadOperations(false).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun activityDbTest() {
        val activity = ActivityEntry(
            ActivityType.Running,
            calories = 50,
            startTime = Calendar.getInstance().timeInMillis
        )

        // insertion test
        activity.id = db.entryManager().insert(activity)
        val byId = db.entryManager().findById(activity.id!!)
        assertEquals(activity, byId)

        // deletion test
        db.entryManager().delete(activity)
        val afterDelete = db.entryManager().findById(activity.id!!)
        assertEquals(afterDelete, null)
    }

}
