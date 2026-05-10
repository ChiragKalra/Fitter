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
import java.util.Calendar

class FriendsFirebaseDao {
	private val reference = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users")

	fun String.toUsernameSet(): MutableSet<String> {
		return split(',')
			.filter { it.isNotBlank() }
			.toMutableSet()
	}

	private val defaultStatsPacket = hashMapOf(
		"steps" to 0,
		"calories" to 0,
		"duration" to 0,
		"distance" to 0,
	)

	private fun intField(ref: DataSnapshot, key: String, default: Int): Int {
		val v = ref.child(key).value ?: return default
		return when (v) {
			is Int -> v
			is Long -> v.toInt()
			is Double -> v.toInt()
			else -> default
		}
	}

	private fun statsFrom(ref: DataSnapshot): HashMap<String, Int> {
		if (!ref.exists()) return defaultStatsPacket
		return hashMapOf(
			"steps" to intField(ref, "steps", defaultStatsPacket["steps"]!!),
			"calories" to intField(ref, "calories", defaultStatsPacket["calories"]!!),
			"duration" to intField(ref, "duration", defaultStatsPacket["duration"]!!),
			"distance" to intField(ref, "distance", defaultStatsPacket["distance"]!!),
		)
	}

	private fun extractFriends(usernames: MutableSet<String>, snapshot: DataSnapshot): MutableList<Friend> {
		val friends = snapshot.children.mapNotNull { ref ->
			val username = ref.child("username").getValue<String>() ?: return@mapNotNull null
			if (username !in usernames) return@mapNotNull null
			val uid = ref.key ?: return@mapNotNull null
			val daily = with(statsFrom(ref.child("daily"))) {
				DailyStats(
					totalSteps = this["steps"]!!,
					totalCalories = this["calories"]!!,
					totalDuration = this["duration"]!!,
					totalDistance = this["distance"]!! / 1000f,
				)
			}
			val weekly = with(statsFrom(ref.child("weekly"))) {
				WeeklyStats(
					totalSteps = this["steps"]!!,
					totalCalories = this["calories"]!!,
					totalDuration = this["duration"]!!,
					totalDistance = this["distance"]!! / 1000f,
				)
			}
			val monthly = with(statsFrom(ref.child("monthly"))) {
				MonthlyStats(
					totalSteps = this["steps"]!!,
					totalCalories = this["calories"]!!,
					totalDuration = this["duration"]!!,
					totalDistance = this["distance"]!! / 1000f,
				)
			}
			Friend(
				uid = uid,
				username = username,
				dailyActivity = daily,
				weeklyActivity = weekly,
				monthlyActivity = monthly,
			)
		}
		return friends.toMutableList()
	}

	private var previousListener: ValueEventListener? = null

	fun flowFriendsDailyActivity(userUid: String, callback: (list: List<Friend>) -> Unit) {
		if (previousListener != null) {
			reference.removeEventListener(previousListener!!)
		}
		previousListener = object: ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val usernamesStr = snapshot.child(userUid).child("friends")
					.getValue<String>() ?: return
				val mUsername = snapshot.child(userUid).child("username")
					.getValue<String>() ?: return
				val usernames = usernamesStr.toUsernameSet().apply {
					add(mUsername)
				}
				callback(extractFriends(usernames, snapshot))
			}
			override fun onCancelled(error: DatabaseError) {}
		}
		reference.addValueEventListener(previousListener!!)
	}

	fun updateUserStatistics(
		userUid: String,
		dailyStats: DailyStats,
		weeklyStats: WeeklyStats,
		monthlyStats: MonthlyStats
	) {
		val userRef = reference.child(userUid)
		userRef
			.child("daily")
			.setValue(
				mapOf(
					"steps" to dailyStats.totalSteps,
					"calories" to dailyStats.totalCalories,
					"duration" to dailyStats.totalDuration,
					"distance" to dailyStats.totalDistance
				)
			)
		userRef
			.child("weekly")
			.setValue(
				mapOf(
					"steps" to weeklyStats.totalSteps,
					"calories" to weeklyStats.totalCalories,
					"duration" to weeklyStats.totalDuration,
					"distance" to weeklyStats.totalDistance
				)
			)
		userRef
			.child("monthly")
			.setValue(
				mapOf(
					"steps" to monthlyStats.totalSteps,
					"calories" to monthlyStats.totalCalories,
					"duration" to monthlyStats.totalDuration,
					"distance" to monthlyStats.totalDistance
				)
			)
		userRef.child("updated").setValue(Calendar.getInstance().timeInMillis)
	}
}
