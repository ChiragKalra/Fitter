package com.bruhascended.fitapp.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.fitapp.ui.friends.AuthHelper.AuthState
import com.bruhascended.fitapp.ui.friends.types.FriendStatistics
import com.bruhascended.fitapp.ui.friends.types.Statistic
import com.bruhascended.fitapp.ui.friends.types.TimePeriod
import com.bruhascended.fitapp.ui.theme.FitAppTheme

class FriendsFragment : Fragment() {

    private lateinit var authHelper: AuthHelper

    private val viewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        authHelper = AuthHelper(this, viewModel)
        val view = ComposeView(requireContext()).apply {
            setContent {
                FitAppTheme {
                    Root()
                }
            }
        }
        return view
    }

    @Composable
    fun Root() {
        var authState by remember {
            mutableStateOf(authHelper.authState)
        }
        authHelper.onAuthStateChange {
            authState = it
        }
        when (authState) {
            AuthState.Unauthorised ->
                GoogleAuth {
                    authHelper.launchAuth()
                }
            AuthState.Authorising ->
                AuthorisingWithGoogle()
            AuthState.UsernameNotSet ->
                SetUsername(authHelper)
            AuthState.SettingUsername ->
                SettingUpProfile()
            AuthState.Authorised ->
                Friends()
        }
    }

    @Preview
    @Composable
    fun Friends() {
        var timePeriod by remember { mutableStateOf(TimePeriod.Daily) }
        var statistic by remember { mutableStateOf(Statistic.TimeActive) }
        var friendStats by remember { mutableStateOf(listOf<FriendStatistics>())}

        viewModel.flowFriendStats(timePeriod, statistic) {
            friendStats = it
        }

        Column {
            TimePeriodTabRow(
                timePeriod = timePeriod,
                onUpdate = {
                    timePeriod = it
                }
            )
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    StatisticTypeRow(
                        statistic = statistic,
                        onUpdate = {
                            statistic = it
                        }
                    )
                }
                if (friendStats.isNotEmpty()) {
                    itemsIndexed(friendStats) { index, item ->
                        FriendStatisticsRow(
                            rank = index + 1,
                            statistics = item,
                            statistic = statistic
                        )
                    }
                } else {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(32.dp)
                        )
                    }
                }
            }
        }
    }

}
