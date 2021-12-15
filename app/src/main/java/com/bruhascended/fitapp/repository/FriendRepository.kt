package com.bruhascended.fitapp.repository

import android.app.Application
import com.bruhascended.db.friends.FriendDatabaseFactory
import kotlin.reflect.KProperty

class FriendRepository(
    mApp: Application
) {

    companion object {
        private var repository: FriendRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FriendRepository {
            if (repository == null) {
                repository = FriendRepository(app)
            }
            return repository!!
        }
    }

    private val db = FriendDatabaseFactory(mApp).build()



}
