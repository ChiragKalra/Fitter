package com.bruhascended.fitapp.ui.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bruhascended.fitapp.repository.FriendRepository

class FriendsViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val friendRepository by FriendRepository.Delegate(mApp)

}