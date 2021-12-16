package com.bruhascended.fitapp.ui.friends

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bruhascended.fitapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthHelper (
	mFragment: Fragment,
	private val viewModel: FriendsViewModel
) {

	enum class AuthState {
		Unauthorised,
		Authorising,
		UsernameNotSet,
		SettingUsername,
		Authorised
	}

	private val TAG = "MY AUTH"

	private var auth: FirebaseAuth = FirebaseAuth.getInstance()
	private var googleSignInClient: GoogleSignInClient
	private var authStateCallback: ((newState: AuthState) -> Unit)? = null
	var previousUsername: String? = null

	private val startForResult = mFragment.registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result: ActivityResult ->
		if (result.resultCode == Activity.RESULT_OK) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
			try {
				val account = task.getResult(ApiException::class.java)
				firebaseAuthWithGoogle(account.idToken!!)
			} catch (e: ApiException) {
				authStateCallback?.invoke(AuthState.Unauthorised)
			}
		}
	}

	init {
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(mFragment.getString(R.string.web_client_id))
			.requestEmail()
			.build()

		googleSignInClient = GoogleSignIn.getClient(mFragment.requireContext(), gso)
	}

	private val isAuthenticated: Boolean
		get() = auth.currentUser != null

	val authState: AuthState
		get() = if (isAuthenticated)
			AuthState.Authorised else AuthState.Unauthorised

	fun launchAuth() {
		authStateCallback?.invoke(AuthState.Authorising)
		startForResult.launch(googleSignInClient.signInIntent)
	}

	fun setUsername(username: String, onComplete: (success: Boolean) -> Unit) {
		val uid = auth.currentUser?.uid
		authStateCallback?.invoke(AuthState.SettingUsername)
		if (uid != null) {
			viewModel.usersRepository.setUsername(uid, username) {
				onComplete(it)
				if (it)
					authStateCallback?.invoke(AuthState.Authorised)
				else
					authStateCallback?.invoke(AuthState.UsernameNotSet)
			}
		} else {
			authStateCallback?.invoke(AuthState.Unauthorised)
		}
	}

	private fun getUsername(callback: (username: String?) -> Unit) {
		val uid = auth.currentUser?.uid
		if (uid != null) {
			viewModel.usersRepository.getUsername(uid) {
				callback(it)
			}
		} else {
			authStateCallback?.invoke(AuthState.Unauthorised)
		}
	}

	fun onAuthStateChange(callback: (newState: AuthState) -> Unit) {
		authStateCallback = callback
	}

	private fun firebaseAuthWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					getUsername {
						previousUsername = it
						authStateCallback?.invoke(AuthState.UsernameNotSet)
					}
				} else {
					authStateCallback?.invoke(AuthState.Unauthorised)
				}
			}
	}

	fun isValidUsername(name: String) =
		viewModel.usersRepository.isValidUsername(name)
}
