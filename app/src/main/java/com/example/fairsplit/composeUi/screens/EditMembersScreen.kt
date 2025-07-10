package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fairsplit.R
import com.example.fairsplit.composeUi.dialogs.AddUserDialog
import com.example.fairsplit.composeUi.components.UserCard


import com.example.fairsplit.data.User
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMembersScreen(
    gVM: GroupViewModel,
    groupID: String,
    onBack: () -> Unit,
    uVM: UserViewModel,
    onAddFriends: () -> Unit,
) {
    uVM.loadFriendsUsers()
    var adminID: String = ""
    var showDialog by remember { mutableStateOf(false) }
    var allUsers: List<User> = listOf()
    var admin by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(Unit) {
        gVM.getUsersInGroup(
            groupId = groupID,
            onSuccess = { users ->
                for (user in users) {
                    gVM.addUser(user)
                }
                admin = users.find { it.id == adminID }
            },
            onFailure = {}
        )
        gVM.getFriendUsers(
            "",
            onSuccess = { users ->
                allUsers = users
                gVM.getGroupByID(groupID) { group ->
                    adminID = group.creator ?: ""
                    admin = allUsers.find { it.id == adminID }
                }
            },
            friendList = uVM.friendsUsers
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.padding(16.dp),
        topBar = {
            TopAppBar(
                title = { Text("Mitglieder verwalten") },
                navigationIcon = {
                    IconButton(onClick = {
                        val membersToAdd: MutableList<String> = gVM.selectedUsers.map { it.id }.toMutableList()
                        gVM.addMembersToGroup(membersToAdd = membersToAdd,onSuccess = {onBack()})
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showDialog) {
            AddUserDialog(
                gVM = gVM,
                showDialog = true,
                onDismiss = { showDialog = false },
                onUserSelected = { user ->
                    gVM.addUser(user)
                },
                onAddFriends = { onAddFriends() },
                uVM = uVM
            )
        }
        Column(modifier = Modifier.padding(innerPadding)) {
            admin?.let {
                Text("Admin")
                UserCard(user = it, onClick = {}, showDeleteIcon = false)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mitglieder")

                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus_icon),
                        contentDescription = "Add to group",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                items(
                    gVM.selectedUsers, key = { it.id }
                ) { user ->
                    if (user.id != adminID) {
                        UserCard(
                            user = user,
                            onClick = {
                                //Wenn der User keine Zahlungen mehr hat und nicht der Ersteller ist, dann lösche ihn aus der Gruppe
                                if (gVM.UserSaveToDelete(user = user)) {
                                    gVM.removeMemberFromGroup(
                                        groupId = groupID,
                                        member = user.id,
                                        onSuccess = {
                                            gVM.selectedUsers.remove(user)
                                        },
                                        onFailure = { e ->
                                            showSnackbar(
                                                e.message ?: "Unbekannter Fehler"
                                            )
                                        }
                                    )
                                } else {
                                    showSnackbar("Mitglied hat noch Schulden oder bekommt Geld.")
                                }
                            },
                            showDeleteIcon = gVM.UserSaveToDelete(user = user)
                        )
                    }
                }
            }
        }
    }
}