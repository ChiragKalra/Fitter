package com.bruhascended.fitapp.ui.addfriends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.fitapp.repository.UsersRepository

class AddFriendsViewModel (mApp: Application) : AndroidViewModel(mApp) {

    val usersRepository by UsersRepository.Delegate()

}