package com.example.fairsplit.data

import com.example.fairsplit.R


data class Group(
    var name: String = "",
    var picture: Int = R.drawable.disneyland,
    var description: String? = null,
    var creator: String? = null,
    var members: MutableList<String> = mutableListOf(),
    var id: String = ""
)
