package com.example.fairsplit.view.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase

class RegistrationViewModel(private val db: Firebase) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var registrationError by mutableStateOf<String?>(null)

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        isLoading = true
        registrationError = null

        db.createUser(
            name,
            email,
            password,
            onSuccess = { user ->
                isLoading = false
                onSuccess(user)
            },
            onFailure = { e ->
                isLoading = false
                registrationError = e.message
                onFailure(e)
            }
        )
    }

    fun passwordEvaluator(password: String): Pair<Boolean, String> {
        val result = when {
            password.length < 6 -> false to "Passwort nicht lang genug"
            !password.any { it.isDigit() } -> false to "Passwort muss Nummer beinhalten"
            !password.any { it.isUpperCase() } -> false to "Passwort muss Großbuchstaben beinhalten"
            !password.any { !it.isLetterOrDigit() } -> false to "Passwort muss Sonderzeichen beinhalten"
            else -> true to "Gutes Passwort"
        }
        Log.d("regVM", "Password evaluation: ${result.second}")
        return result
    }
}