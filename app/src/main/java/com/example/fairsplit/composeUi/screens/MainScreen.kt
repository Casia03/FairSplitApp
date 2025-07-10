package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import com.example.fairsplit.ui.theme.background
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.LoginViewModel
import com.example.fairsplit.view.model.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uVM: UserViewModel,
    gVM: GroupViewModel,
    lVM: LoginViewModel,
    onAddGroupClick: () -> Unit,
    onAddSpendingClick: () -> Unit,
    onGroupDetailsClick: (String) -> Unit,
    onSignOut: () -> Unit,
    onFriendList: () -> Unit,
)
{
    var selectedTab by remember { mutableStateOf("groups") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            "groups" -> "FairSplit"
                            "activities" -> "Aktivitäten"
                            "profile" -> "Profil"
                            else -> ""
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar (/*modifier = Modifier.background(color = background),*/
                containerColor = background){
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Gruppen") },
                    selected = selectedTab == "groups",
                    onClick = { selectedTab = "groups" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Aktivitäten") },
                    selected = selectedTab == "activities",
                    onClick = { selectedTab = "activities" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profil") },
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "groups" -> GroupScreen(gVM, onAddGroupClick, onGroupDetailsClick, onAddSpendingClick)
                "activities" -> ActivityScreen(gVM)
                "profile" -> ProfileScreen(uVM, lVM, onSignOut, onFriendList)
            }
        }
    }
}