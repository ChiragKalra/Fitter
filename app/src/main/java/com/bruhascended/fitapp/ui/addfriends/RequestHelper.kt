package com.bruhascended.fitapp.ui.addfriends

import com.google.firebase.auth.FirebaseAuth

class RequestHelper(
	private val viewModel: AddFriendsViewModel
) {
	private var firebaseAuthUid = FirebaseAuth.getInstance().uid!!

	fun onRequestsUpdate(onUpdate: (requests: List<String>) -> Unit) {
		viewModel.usersRepository.flowFriendRequests(firebaseAuthUid) {
			onUpdate(it)
		}
	}

	fun acceptRequest(friendUsername: String) {
		viewModel.usersRepository.acceptRequest(firebaseAuthUid, friendUsername)
	}

	fun denyRequest(friendUsername: String) {
		viewModel.usersRepository.denyRequest(firebaseAuthUid, friendUsername)
	}

	fun sendRequest(friendUsername: String, onCompleted: (successful: Boolean) -> Unit) {
		viewModel.usersRepository.sendRequest(firebaseAuthUid, friendUsername, onCompleted)
	}
}