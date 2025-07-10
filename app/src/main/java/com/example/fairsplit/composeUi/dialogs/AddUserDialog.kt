package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fairsplit.data.User
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.UserViewModel
import com.example.fairsplit.composeUi.components.UserCard

@Composable
fun AddUserDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    gVM: GroupViewModel,
    uVM: UserViewModel,
    onAddFriends: () -> Unit,
) {
    var query by remember { mutableStateOf("") }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, //Schließt sich auch wenn man außerhalb des Dialogs klickt
            confirmButton = {
                Row{
                    //Find Friends
                    Button(onClick = onAddFriends) {
                        Text("Freunde finden")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    //Close Button
                    Button(onClick = onDismiss) {
                        Text("Schließen")
                    }
                }
            },

            title = {
                Text("Benutzer hinzufügen")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Benutzer suchen") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //Choose Users
                    UserListComponent(query = query, onUserSelected = onUserSelected, gVM, uVM)
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UserListComponent(
    query: String,
    onUserSelected: (User) -> Unit,
    gVM: GroupViewModel,
    uVM: UserViewModel
) {
    var searchResult by remember { mutableStateOf<List<User>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        gVM.getFriendUsers(
            query = query,
            uVM.friendsUsers,
            onSuccess = { filteredFriends ->
                val filtered = filteredFriends.filter { u ->
                    gVM.selectedUsers.none { it.id == u.id } && u.id != gVM.currentUserId.value
                }
                searchResult = filtered
            },
        )
    }


    when {
        searchResult == null -> {
            CircularProgressIndicator()
        }

        else -> {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                items(searchResult!!, key = { it.id }) { user ->
                    UserCard(user = user, onClick = {
                        onUserSelected(user)
                        searchResult = searchResult?.filter { it.id != user.id }
                })
                }
            }
        }
    }
}

