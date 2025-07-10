package com.example.fairsplit.session

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase


//Manages the current user session, including login, logout, and session persistence.

class SessionManager(private val db: Firebase) {
    var currentUser = mutableStateOf<User?>(null)
        private set

    fun setCurrentUser(user: User?) {
        currentUser.value = user
    }

    fun login(context: Context, user: User) {
        Log.d("SessionManager","Current User before assigning: " + currentUser)
        currentUser.value = user
        Log.d("SessionManager","Current User after assigning: " + currentUser)
        saveUserJsonToPrefs(context, user)
    }

    fun logout(context: Context) {
        currentUser.value = null
        clearPrefs(context)
    }

    fun loadSession(context: Context, onUserLoaded: (User?) -> Unit) {
        val uid = getUserIdFromPrefs(context)
        Log.d("SessionManager" ,"the uid value equals: " + uid)
        if (uid != null) {
            db.getUserByIdLogin(uid) { user ->
                currentUser.value = user
                onUserLoaded(user)
            }
        } else {
            onUserLoaded(null)
        }
    }

    fun getCurrentUser(): User? = currentUser.value
    fun isLoggedIn(): Boolean = currentUser != null

    private val PREF_NAME = "user_session"
    private val KEY_USER_ID = "user_id"

    private fun saveUserJsonToPrefs(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        Log.d("SessionManager","saved preferences: " + prefs)
        prefs.edit().putString(KEY_USER_ID, user.id).apply()
    }

    private fun getUserIdFromPrefs(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    private fun clearPrefs(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
