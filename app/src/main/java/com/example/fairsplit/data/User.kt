package com.example.fairsplit.data

import com.example.fairsplit.R


data class User (
    var id: String = "",
    var name: String = "",
    var name_lowercase: String = "", // Lowercase für angenehme freundesuche
    var passwort: String = "", //SECRET
    var email: String = "",
    var friends: MutableList<String> = mutableListOf(), // Liste der Freunde ??
    var icon: Int = R.drawable.avatar //Default Icon wegen in der Doku erwähntem Speicherproblem
)