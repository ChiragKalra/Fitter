package com.bruhascended.fitapp.firebase

import com.bruhascended.db.friends.entities.DailyStats
import com.bruhascended.db.friends.entities.Friend
import com.bruhascended.db.friends.entities.MonthlyStats
import com.bruhascended.db.friends.entities.WeeklyStats
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class FriendsFirebaseDao {
	private val reference = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users")

	private fun extractFriend(uid: String, snapshot: DataSnapshot): Friend {
		val ref = snapshot.child(uid)
		val username = ref.child("username").getValue<String>()!!
		val daily = with(
			ref.child("daily").getValue<HashMap<String, Int>>()!!
		) {
			DailyStats(
				totalSteps = this["steps"]!!,
				totalCalories = this["calories"]!!,
				totalDuration = this["duration"]!!,
				totalDistance = this["distance"]!!/1000f,
			)
		}
		val weekly = with(
			ref.child("weekly").getValue<HashMap<String, Int>>()!!
		) {
			WeeklyStats(
				totalSteps = this["steps"]!!,
				totalCalories = this["calories"]!!,
				totalDuration = this["duration"]!!,
				totalDistance = this["distance"]!!/1000f,
			)
		}
		val monthly = with(
			ref.child("monthly").getValue<HashMap<String, Int>>()!!
		) {
			MonthlyStats(
				totalSteps = this["steps"]!!,
				totalCalories = this["calories"]!!,
				totalDuration = this["duration"]!!,
				totalDistance = this["distance"]!!/1000f,
			)
		}
		return Friend(
			uid = uid,
			username = username,
			dailyActivity = daily,
			weeklyActivity = weekly,
			monthlyActivity = monthly,
		)
	}

	fun flowFriendsDailyActivity(userUid: String, callback: (list: List<Friend>) -> Unit) {
		reference.addValueEventListener(object: ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val friendsUid = snapshot.child(userUid).child("friends")
					.getValue<List<String>>() ?: return
				val list = mutableListOf<Friend>()
				for (uid in friendsUid) {
					list.add(extractFriend(uid, snapshot))
				}
				callback(list)
			}
			override fun onCancelled(error: DatabaseError) {}
		})
	}
}
