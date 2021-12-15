package com.bruhascended.fitapp.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.friends.AuthHelper.AuthState

class FriendsFragment : Fragment() {

    private lateinit var authHelper: AuthHelper

    private val viewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        authHelper = AuthHelper(this, viewModel)
        val view = ComposeView(requireContext()).apply {
            setContent {
                Root()
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
                    authHelper.setUsername(name)
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
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.friendRepository.getAll() },
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.friend_requests),
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }
}
