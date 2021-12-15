package com.bruhascended.fitapp.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class FriendsFirebaseDao {
	private val reference = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users")

	fun getAll() {
		Log.d("finding",  "NULL")
		reference.addListenerForSingleValueEvent(object: ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				Log.d("found", snapshot.key?: "NULL")
			}
			override fun onCancelled(error: DatabaseError) {
				Log.d("not found", "NULL")
			}
		})
	}
}
