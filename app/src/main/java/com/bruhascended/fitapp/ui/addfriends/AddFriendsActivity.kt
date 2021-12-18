package com.bruhascended.fitapp.ui.addfriends

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.theme.FitAppTheme

class AddFriendsActivity : ComponentActivity() {

	private val viewModel: AddFriendsViewModel by viewModels()
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
				Screen()
			}
		}
	}

	@Preview(showBackground = true)
	@Composable
	fun Screen() {
		Column {
			TopAppBar(
				title = {
					Text(stringResource(R.string.add_friends))
				},
				navigationIcon = {
					IconButton(
						onClick = { finish() }
					) {
						Icon(
							Icons.Filled.ArrowBack,
							contentDescription = stringResource(R.string.back)
						)
					}
				 },
			)
			Root()
		}
	}

	@Composable
	fun Root() {
		var requests by remember { mutableStateOf<List<String>?>(null) }
		requestHelper.onRequestsUpdate {
			requests = it
		}
		LazyColumn (
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.padding(12.dp)
		) {
			item {
				LabelText(
					stringResource(R.string.send_request)
				)
			}
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
							text =
								stringResource(R.string.no_requests),
							fontSize = 22.sp,
							fontWeight = FontWeight.Light
						)
					}
				}
				else -> {
					item {
						LabelText(
							stringResource(R.string.friend_requests)
						)
					}
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
	fun LabelText(text: String) {
		Text(
			text = text,
			textAlign = TextAlign.Left,
			fontSize = 18.sp,
			fontWeight = FontWeight.SemiBold,
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 12.dp),
		)
	}

	@Composable
	fun RequestItem(username: String) {
		Row (
			modifier = Modifier
				.padding(
					horizontal = 12.dp,
					vertical = 8.dp,
				),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = username,
				modifier = Modifier
					.weight(1f)
					.padding(
						horizontal = 8.dp,
					),
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
		Row (
			modifier = Modifier
				.padding(
					top = 12.dp,
					bottom = 32.dp,
				),
			verticalAlignment = Alignment.CenterVertically
		) {
			OutlinedTextField(
				value = username,
				placeholder = {
					Text(stringResource(R.string.enter_username))
				},
				onValueChange = {
					username = it
				},
				modifier = Modifier.weight(1f)
			)
			IconButton(
				modifier = Modifier.size(48.dp),
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
