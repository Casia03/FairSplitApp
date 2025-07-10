package com.example.fairsplit.view.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fairsplit.session.SessionManager
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase


class LoginViewModel(private val db : Firebase, private val sm : SessionManager) : ViewModel() {
    var searchResults by mutableStateOf<List<User>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    fun loginUser(
        context: Context,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.loginUser(
            email,
            password,
            onSuccess = { user ->
                sm.login(context, user)
                currentUser = user
                onSuccess(user)
            },
            onFailure = { e ->
                errorMessage = e.message
                onFailure(e)
            }
        )
    }

    fun logout(context: Context) {
        sm.logout(context)
        currentUser = null
    }

    fun searchUsers(name: String) {
        db.getUsers(
            name,
            onSuccess = { users ->
                searchResults = users
                Log.d("Firestore", "Search results for \"$name\": $users")
            },
            onFailure = { e ->
                errorMessage = e.message
                Log.e("Firestore", "Error while searching for \"$name\"", e)
            }
        )
    }
}