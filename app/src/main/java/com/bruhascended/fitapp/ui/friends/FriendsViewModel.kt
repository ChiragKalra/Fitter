package com.bruhascended.fitapp.ui.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.fitapp.repository.FriendsRepository
import com.bruhascended.fitapp.repository.UsersRepository

class FriendsViewModel (mApp: Application) : AndroidViewModel(mApp) {

    val friendRepository by FriendsRepository.Delegate(mApp)
    val usersRepository by UsersRepository.Delegate(mApp)

}