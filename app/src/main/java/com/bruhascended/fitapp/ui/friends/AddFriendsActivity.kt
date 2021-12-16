package com.bruhascended.fitapp.ui.friends

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.theme.FitAppTheme

class AddFriendsActivity : ComponentActivity() {

	private val viewModel: FriendsViewModel by viewModels()
	private lateinit var requestHelper: RequestHelper

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestHelper = RequestHelper(viewModel)
		setContent {
			FitAppTheme {
				Root()
			}
		}
	}

	@Preview(showBackground = true)
	@Composable
	fun Root() {
		var requests by remember { mutableStateOf<List<String>?>(null) }
		requestHelper.onRequestsUpdate {
			requests = it
		}
		LazyColumn {
			item {
				SendRequestField()
			}
			when {
				requests == null -> {
					item {
						CircularProgressIndicator()
					}
				}
				requests?.isEmpty() ?: true -> {
					item {
						Text(
							stringResource(R.string.no_requests)
						)
					}
				}
				else -> {
					items(requests!!) { username ->
						RequestItem(
							username = username,
						)
					}
				}
			}
		}
	}

	@Composable
	fun RequestItem(username: String) {
		Row {
			Text(
				text = username,
				modifier = Modifier.weight(1f)
			)
			IconButton(
				onClick = {
					requestHelper.denyRequest(username)
				}
			) {
				Icon(
					Icons.Rounded.Close,
					contentDescription = stringResource(R.string.deny_request)
				)
			}
			IconButton(
				onClick = {
					requestHelper.acceptRequest(username)
				}
			) {
				Icon(
					Icons.Rounded.Done,
					contentDescription = stringResource(R.string.accept_request)
				)
			}
		}
	}

	@Composable
	fun SendRequestField() {
		var username by remember { mutableStateOf("") }
		Row {
			OutlinedTextField(
				value = username,
				onValueChange = {
					username = it
				},
				modifier = Modifier.weight(1f)
			)
			IconButton(
				onClick = {
					requestHelper.sendRequest(username) { successful ->
						if (successful) {
							username = ""
							showShortToast(R.string.request_sent)
						} else {
							showShortToast(R.string.invalid_username)
						}
					}
				}
			) {
				Icon(
					Icons.Rounded.Send,
					contentDescription = stringResource(R.string.send_request)
				)
			}
		}
	}
}
