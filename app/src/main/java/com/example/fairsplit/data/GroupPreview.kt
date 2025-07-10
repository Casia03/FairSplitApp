package com.example.fairsplit.data

import com.example.fairsplit.R


//Derzeit nicht benutzt: Zeigt die letzten drei Spendings auf der GroupCard im GorupScreen an


data class GroupPreview (
    val id: String,
    val name: String,
    val picture: Int = R.drawable.disneyland,
    val lastSpendings: List<Spending> = emptyList(),
    val transferList: List<Triple<String,String,Long>> = emptyList()
)