package com.bruhascended.fitapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class FriendsFirebaseDao {
	private val reference = FirebaseDatabase.getInstance().getReference("users")

}
