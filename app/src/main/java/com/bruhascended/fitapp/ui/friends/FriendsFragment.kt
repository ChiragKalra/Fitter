package com.bruhascended.fitapp.ui.friends

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.friends.AuthHelper.AuthState
import com.bruhascended.fitapp.ui.friends.types.FriendStatistics
import com.bruhascended.fitapp.ui.friends.types.Statistic
import com.bruhascended.fitapp.ui.friends.types.TimePeriod
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import java.sql.Time

class FriendsFragment : Fragment() {

    private lateinit var authHelper: AuthHelper

    private val viewModel: FriendsViewModel by viewModels()

    private fun Context.showShortToast(
        @StringRes
        stringRes: Int
    ) {
        Toast.makeText(
            this,
            getString(stringRes),
            Toast.LENGTH_SHORT
        ).show()
    }

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

    @Preview
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
                GoogleAuth()
            AuthState.Authorising ->
                AuthorisingWithGoogle()
            AuthState.UsernameNotSet ->
                SetUsername()
            AuthState.SettingUsername ->
                SettingUpProfile()
            AuthState.Authorised ->
                Friends()
        }
    }

    @Composable
    fun GoogleAuth() {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.sign_in_for_friends),
                modifier = Modifier.padding(32.dp),
                fontSize = 22.sp,
                color = MaterialTheme.colors.primary
            )
            Button(
                onClick = {
                    authHelper.launchAuth()
                },
                modifier = Modifier
            ) {
                Text(
                    text = stringResource(R.string.google_auth),
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }

    @Composable
    fun AuthorisingWithGoogle() {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.signing_in),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.h5
            )
        }
    }

    @Composable
    fun SetUsername() {
        val context = LocalContext.current
        Column(modifier = Modifier.padding(16.dp)) {
            var name by remember { mutableStateOf(authHelper.previousUsername ?: "") }
            if (name.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.select_a_username),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.h5
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
            Button(
                onClick = {
                    if (authHelper.isValidUsername(name)) {
                        authHelper.setUsername(name) { successful ->
                            if (!successful) {
                                context.showShortToast(R.string.username_taken)
                            }
                        }
                    } else {
                        context.showShortToast(R.string.invalid_username)
                    }
                },
                modifier = Modifier
            ) {
                Text(
                    text = stringResource(R.string.google_auth),
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }

    @Composable
    fun SettingUpProfile() {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.setting_up_profile),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.h5
            )
        }
    }

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
            StatisticTypeRow(
                statistic = statistic,
                onUpdate = {
                    statistic = it
                }
            )
            LazyColumn {
                itemsIndexed(friendStats) { index, item ->
                    FriendStatisticsRow(
                        rank = index + 1,
                        statistics = item,
                        statistic = statistic
                    )
                }
            }
        }
    }

    @Composable
    fun TimePeriodTabRow(
        timePeriod: TimePeriod,
        onUpdate: (timePeriod: TimePeriod) -> Unit
    ) {
        TabRow(
            selectedTabIndex = TimePeriod.values().indexOf(timePeriod)
        ) {
            TimePeriod.values().forEach {
                Tab(
                    selected = it == timePeriod,
                    onClick = { onUpdate(it) }
                ) {
                    Text(
                        text = stringResource(it.stringRes)
                    )
                }
            }
        }
    }

    @Composable
    fun StatisticTypeRow(
        statistic: Statistic,
        onUpdate: (statistic: Statistic) -> Unit
    ) {
        Row {
            Statistic.values().forEach {
                Button(
                    onClick = { onUpdate(it) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(it.stringRes)
                    )
                }
            }
        }
    }

    @Composable
    fun FriendStatisticsRow(
        rank: Int,
        statistics: FriendStatistics,
        statistic: Statistic
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = rank.toString()
            )
            Text(
                text = statistics.username,
                modifier = Modifier
                    .background(Color.Red)
                    .weight(1f)
                    .padding(8.dp)
            )
            Text(
                text = when(statistic) {
                    Statistic.DistanceCovered -> statistics.totalDistance
                    Statistic.StepsTaken -> statistics.totalSteps
                    Statistic.TimeActive -> statistics.totalDuration
                }.toString()
            )
        }
    }


}
