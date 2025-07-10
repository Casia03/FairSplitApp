package com.example.fairsplit.view.model

import androidx.compose.runtime.*
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase
import com.example.fairsplit.session.SessionManager

class UserViewModel(private var sm: SessionManager, private val db: Firebase) : ViewModel() {
    val selectedUsers = mutableStateListOf<User>()

    val user: State<User?> get() = sm.currentUser

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val friendsUsers = mutableStateListOf<User>()

    val friendsList: MutableList<String>?
        get() = user.value?.friends
    val name: String
        get() = user.value?.name ?: ""
    val name_lowercase: String
        get() = user.value?.name_lowercase ?: ""
    val email: String
        get() = user.value?.email ?: ""

    fun searchUsers(
        query: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        db.getUsers(
            query = query,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun addFriend(friend: User) {
        val currentUser = user.value ?: return
        selectedUsers.add(friend)
        db.addFriendsToUser(
            user.value?.id ?: return,
            friend.id,
            onSuccess = {
                val updatedFriends = currentUser.friends.toMutableList().apply {
                    add(friend.id)
                    Log.d("UserViewModel", "User ${friend.id} added to friends")
                }
                updateCurrentUserFriends(updatedFriends)
            },
            onFailure = { exception ->
                // handle error, e.g., log or show message
                Log.e("UserViewModel", "Failed to add friend: ${exception.message}")
            }
        )
    }

    fun loadFriendsUsers() {
        val ids = friendsList
        Log.d("UserViewModel", "Current friend id list: " + friendsList)
        friendsUsers.clear()
        ids?.forEach { id ->
            db.getUserById(
                id,
                onSuccess = { user ->
                    friendsUsers.add(user)
                },
                onFailure = { e ->
                    errorMessage = e.message
                    //                    onFailure(e)
                }
            )
        }
    }

    fun removeFriend(friend: User) {
        val currentUser = user.value ?: return
        selectedUsers.remove(friend)
        db.removeFriendsFromUser(
            currentUser.id,
            friend.id,
            onSuccess = {
                val updatedFriends = currentUser.friends.toMutableList().apply {
                    remove(friend.id)
                }
                updateCurrentUserFriends(updatedFriends)
            },
            onFailure = { exception ->
                errorMessage = exception.message
                Log.e("UserViewModel", "Failed to remove friend: ${exception.message}")
            }
        )
    }

    private fun updateCurrentUserFriends(updatedFriends: MutableList<String>) {
        val currentUser = user.value ?: return
        val updatedUser = currentUser.copy(friends = updatedFriends)
        sm.setCurrentUser(updatedUser)
    }
}