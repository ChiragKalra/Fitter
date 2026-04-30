package com.bruhascended.fitapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class UsersFirebaseDao {
	private val reference = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users")

	fun MutableSet<String>.toUsernameString(): String {
		var str = ""
		forEach {
			str += "$it,"
		}
		return str
	}

	fun String.toUsernameSet(): MutableSet<String> {
		return split(',')
			.filter { it.isNotBlank() }
			.toMutableSet()
	}


	fun getUsernameByUid(
		uid: String, callback: (username: String?) -> Unit
	) {
		reference
			.child(uid)
			.child("username")
			.addListenerForSingleValueEvent(object: ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val value = snapshot.getValue<String?>()
					callback(value)
				}
				override fun onCancelled(error: DatabaseError) {
					callback(null)
				}
			})
	}

	fun getUidByUsername(username: String, callback: (uid: String?) -> Unit) {
		reference
			.orderByChild("username")
			.equalTo(username)
			.limitToFirst(1)
			.addListenerForSingleValueEvent(
				object: ValueEventListener {
					override fun onDataChange(snapshot: DataSnapshot) {
						try {
							val value = snapshot.getValue<HashMap<String, Any?>>()?.keys?.first()
							callback(value)
						} catch (e: Exception) {
							callback(null)
						}
					}
					override fun onCancelled(error: DatabaseError) {
						callback(null)
					}
				}
			)
	}

	fun setUsername(
		uid: String, username: String, callback: ((successful: Boolean) -> Unit)? = null
	) {
		getUidByUsername(username) {
			when (it) {
				null -> {
					reference
						.child(uid)
						.setValue(
							mapOf(
								"username" to username,
								"requests" to "",
								"friends" to "",
							)
						)
						.addOnSuccessListener { it: Void? ->
							callback?.invoke(true)
						}
						.addOnFailureListener { it: Exception? ->
							callback?.invoke(false)
						}
				}
				uid -> {
					callback?.invoke(true)
				}
				else -> {
					callback?.invoke(false)
				}
			}
		}
	}

	fun flowFriendRequests(userUid: String, onUpdate: (list: List<String>) -> Unit) {
		reference
			.child(userUid)
			.child("requests")
			.addValueEventListener(object: ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val usernames = snapshot.getValue<String>() ?: return
					val usernameList = usernames
						.toUsernameSet()
						.toList()
					onUpdate(usernameList)
				}
				override fun onCancelled(error: DatabaseError) {
					onUpdate(emptyList())
				}
			})
	}


	fun addToFriends(userUid: String, friendUsername: String) {
		val friendsRef = reference
			.child(userUid)
			.child("friends")

		friendsRef.addListenerForSingleValueEvent(object: ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val usernames = snapshot.getValue<String>() ?: return
					val newFriendsString = usernames
						.toUsernameSet()
						.apply {
							add(friendUsername)
						}
						.toUsernameString()
					friendsRef.setValue(newFriendsString)
				}
				override fun onCancelled(error: DatabaseError) {}
			})
	}

	fun addRequest(
		userUid: String,
		requestUsername: String
	) {
		val requestsRef = reference
			.child(userUid)
			.child("requests")

		requestsRef.addListenerForSingleValueEvent(object: ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val usernames = snapshot.getValue<String>() ?: return
				val newRequestsString = usernames
					.toUsernameSet()
					.apply {
						add(requestUsername)
					}
					.toUsernameString()
				requestsRef.setValue(newRequestsString)
			}
			override fun onCancelled(error: DatabaseError) {}
		})
	}

	fun removeRequest(userUid: String, requestUsername: String) {
		val requestsRef = reference
			.child(userUid)
			.child("requests")

		requestsRef.addListenerForSingleValueEvent(object: ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val usernames = snapshot.getValue<String>() ?: return
				val newRequestsString = usernames
					.toUsernameSet()
					.apply {
						remove(requestUsername)
					}
					.toUsernameString()
				requestsRef.setValue(newRequestsString)
			}
			override fun onCancelled(error: DatabaseError) {}
		})
	}
}
