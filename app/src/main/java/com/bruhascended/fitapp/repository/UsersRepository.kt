package com.bruhascended.fitapp.repository

import android.app.Application
import com.bruhascended.db.friends.FriendDatabaseFactory
import com.bruhascended.fitapp.firebase.FriendsFirebaseDao
import com.bruhascended.fitapp.firebase.UsersFirebaseDao
import kotlin.reflect.KProperty

class UsersRepository(
    mApp: Application
) {

    companion object {
        private var repository: UsersRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): UsersRepository {
            if (repository == null) {
                repository = UsersRepository(app)
            }
            return repository!!
        }
    }

    private val firebaseUsersDb = UsersFirebaseDao()

    fun getUsernameWithCallback(
        uid: String, callback: (username: String?) -> Unit
    ) = firebaseUsersDb.getUsernameWithCallback(uid, callback)


    fun setUsernameWithCallback(
        uid: String, username: String, callback: (successful: Boolean) -> Unit
    ) = firebaseUsersDb.setUsernameWithCallback(uid, username, callback)

}
