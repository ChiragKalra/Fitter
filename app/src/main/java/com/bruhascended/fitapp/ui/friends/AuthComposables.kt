package com.bruhascended.fitapp.ui.friends

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.theme.White700


fun Context.showShortToast(
	@StringRes
	stringRes: Int
) {
	Toast.makeText(
		this,
		getString(stringRes),
		Toast.LENGTH_SHORT
	).show()
}

@Composable
fun GoogleAuth(onClick: () -> Unit) {
	Column(
		modifier = Modifier
			.padding(16.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(id = R.string.sign_in_for_friends),
			modifier = Modifier.padding(32.dp),
			fontSize = 22.sp,
			color = MaterialTheme.colors.onPrimary
		)
		Button(
			onClick = onClick,
			modifier = Modifier
		) {
			Text(
				text = stringResource(R.string.google_auth),
				modifier = Modifier.align(Alignment.Top),
				color = White700
			)
		}
	}
}

@Composable
fun AuthorisingWithGoogle() {
	Column(
		modifier = Modifier.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		CircularProgressIndicator()
		Text(
			text = stringResource(R.string.signing_in),
			modifier = Modifier.padding(bottom = 8.dp),
			style = MaterialTheme.typography.h5,
			color = MaterialTheme.colors.onPrimary
		)
	}
}

@Composable
fun SetUsername(authHelper: AuthHelper) {
	val context = LocalContext.current
	Column(
		modifier = Modifier.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		var name by remember { mutableStateOf(authHelper.previousUsername ?: "") }
		Text(
			text = stringResource(R.string.select_a_username),
			modifier = Modifier.padding(bottom = 8.dp),
			style = MaterialTheme.typography.h5,
			color = MaterialTheme.colors.onPrimary
		)
		OutlinedTextField(
			value = name,
			onValueChange = { name = it },
			label = {
				Text(
					text = stringResource(R.string.username),
					color = MaterialTheme.colors.onPrimary
				)
			},
			colors = TextFieldDefaults.outlinedTextFieldColors(
				textColor = MaterialTheme.colors.onPrimary
			)
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
				.padding(12.dp)
		) {
			Text(
				text = stringResource(R.string.continue_text),
				modifier = Modifier.align(Alignment.Top),
				color = White700
			)
		}
	}
}

@Composable
fun SettingUpProfile() {
	Column(
		modifier = Modifier.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		CircularProgressIndicator()
		Text(
			text = stringResource(R.string.setting_up_profile),
			modifier = Modifier.padding(bottom = 8.dp),
			style = MaterialTheme.typography.h5,
			color = MaterialTheme.colors.onPrimary
		)
	}
}

