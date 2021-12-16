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

    fun isValidUsername(username: String): Boolean {
        val reg = Regex("^[a-z0-9A-Z_]{3,20}\$")
        return reg.matches(username)
    }

    fun getUsername(
        uid: String, callback: (username: String?) -> Unit
    ) = firebaseUsersDb.getUsernameByUid(uid, callback)

    fun setUsername(
        uid: String, username: String, callback: (successful: Boolean) -> Unit
    ) = firebaseUsersDb.setUsername(uid, username, callback)

    fun flowFriendRequests(
        userUid: String, onUpdate: (list: List<String>) -> Unit
    ) = firebaseUsersDb.flowFriendRequests(userUid, onUpdate)

    fun acceptRequest(mUid: String, friendUsername: String) {
        firebaseUsersDb.apply {
            // remove request from user's requests
            removeRequest(mUid, friendUsername)
            // add friend to user's friends
            addToFriends(mUid, friendUsername)
            getUidByUsername(friendUsername) { friendUid ->
                getUsernameByUid(mUid) { mUsername ->
                    if (friendUid == null || mUsername == null) return@getUsernameByUid
                    // add user to friend's friends
                    addToFriends(friendUid, mUsername)
                }
            }
        }
    }

    fun denyRequest(mUid: String, friendUsername: String) {
        // remove request from user's requests
        firebaseUsersDb.removeRequest(mUid, friendUsername)
    }

    fun sendRequest(
        mUid: String,
        friendUsername: String,
        onCompleted: (successful: Boolean) -> Unit
    ) {
        if (isValidUsername(friendUsername)) {
            firebaseUsersDb.apply {
                getUidByUsername(friendUsername) { friendUid ->
                    if (friendUid != null) {
                        getUsernameByUid(mUid) { mUsername ->
                            if (mUsername != null) {
                                addRequest(friendUid, mUsername)
                                onCompleted(true)
                            } else {
                                onCompleted(false)
                            }
                        }
                    } else {
                        onCompleted(false)
                    }
                }
            }
        } else {
            onCompleted(false)
        }
    }
}
