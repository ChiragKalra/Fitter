package com.bruhascended.fitapp.ui.friends

import android.app.Activity
import android.util.Log
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
	private val mFragment: Fragment
) {

	private val TAG = "MY AUTH"

	private var auth: FirebaseAuth = FirebaseAuth.getInstance()
	private var googleSignInClient: GoogleSignInClient
	private var authStateCallback: ((isAuthenticated: Boolean) -> Unit)? = null

	init {
		// init firebase

		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(mFragment.getString(R.string.web_client_id))
			.requestEmail()
			.build()

		googleSignInClient = GoogleSignIn.getClient(mFragment.requireContext(), gso)

	}

	val isAuthenticated: Boolean
		get() = auth.currentUser != null

	fun launchAuth() {
		startForResult.launch(googleSignInClient.signInIntent)
	}

	fun onAuthStateChange(call: (isAuthenticated: Boolean) -> Unit) {
		authStateCallback = call
	}

	private fun firebaseAuthWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Log.d(TAG, "signInWithCredential:success")
					authStateCallback?.invoke(true)
				} else {
					Log.w(TAG, "signInWithCredential:failure", task.exception)
					authStateCallback?.invoke(false)
				}
			}
	}

	private val startForResult = mFragment.registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result: ActivityResult ->
		if (result.resultCode == Activity.RESULT_OK) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
			val user = auth.currentUser
			try {
				// Google Sign In was successful, authenticate with Firebase
				val account = task.getResult(ApiException::class.java)
				Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
				firebaseAuthWithGoogle(account.idToken!!)
			} catch (e: ApiException) {
				// Google Sign In failed, update UI appropriately
				Log.w(TAG, "Google sign in failed", e)
			}
		}
	}
}