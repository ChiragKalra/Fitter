package com.bruhascended.fitapp.ui.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.fitapp.repository.FriendsRepository
import com.bruhascended.fitapp.repository.UsersRepository
import com.bruhascended.fitapp.ui.friends.types.FriendStatistics
import com.bruhascended.fitapp.ui.friends.types.Statistic
import com.bruhascended.fitapp.ui.friends.types.TimePeriod
import com.google.firebase.auth.FirebaseAuth

class FriendsViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val friendRepository by FriendsRepository.Delegate()
    val usersRepository by UsersRepository.Delegate()

    private val firebaseAuthUid: String
        get() = FirebaseAuth.getInstance().uid!!

    fun flowFriendStats(
        timePeriod: TimePeriod,
        statistic: Statistic,
        onUpdate: (stats: List<FriendStatistics>) -> Unit
    ) = friendRepository.flowFriendsStatistics(
            firebaseAuthUid,
            timePeriod = timePeriod,
            sortBy = statistic,
            onUpdate
        )
}