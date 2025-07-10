package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.example.fairsplit.view.model.GroupViewModel
//Zeigt alle Aktivitäten an, die mit dem Nutzer zu tun haben (z.B. Gruppe Kino: Du hast 5 Euro für Tickets ausgegeben)
@Composable
fun ActivityScreen(
    gVM: GroupViewModel,
) {
    LaunchedEffect(Unit) {
        gVM.loadAllSpendingsForUser()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = gVM.spendingsFromAllUserGroups,
                key = { it.spendingId }
            ) { spending ->
                SpendingItem(spending, gVM,true)
            }
        }
    }
}
