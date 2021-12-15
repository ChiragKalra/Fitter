package com.bruhascended.fitapp.repository

import android.app.Application
import com.bruhascended.db.friends.FriendDatabaseFactory
import com.bruhascended.fitapp.firebase.FriendsFirebaseDao
import com.bruhascended.fitapp.firebase.UsersFirebaseDao
import kotlin.reflect.KProperty

class FriendsRepository(
    mApp: Application
) {

    companion object {
        private var repository: FriendsRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FriendsRepository {
            if (repository == null) {
                repository = FriendsRepository(app)
            }
            return repository!!
        }
    }

    private val roomDb = FriendDatabaseFactory(mApp).build()
    private val firebaseFriendsDb = FriendsFirebaseDao()

    fun getAll() = firebaseFriendsDb.getAll()
}
