package com.bruhascended.fitapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class UsersFirebaseDao {
	private val reference = FirebaseDatabase.getInstance().getReference("users")

	fun getUsernameWithCallback(
		uid: String, callback: (username: String?) -> Unit
	) {
		reference.child(uid).child("username")
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

	fun setUsernameWithCallback(
		uid: String, username: String, callback: (successful: Boolean) -> Unit
	) {
		reference.child(uid).child("username")
			.setValue(username)
			.addOnSuccessListener {
				callback(true)
			}.addOnCanceledListener {
				callback(false)
			}.addOnFailureListener {
				callback(false)
			}
	}
}
