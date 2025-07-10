package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fairsplit.composeUi.components.UserCard
import com.example.fairsplit.data.User
import com.example.fairsplit.view.model.UserViewModel

@Composable
fun AddFriendDialogComponent(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    viewModel: UserViewModel,
) {
    val user by viewModel.user

    var query by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, //Schließt sich auch wenn man außerhalb des Dialogs klickt
            confirmButton = { //Close Button
                Button(onClick = onDismiss) {
                    Text("Schließen")
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
                    user?.let { UserListComponent(it, query = query, onUserSelected = onUserSelected, viewModel) }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UserListComponent(
    user: User,
    query: String,
    onUserSelected: (User) -> Unit,
    viewModel: UserViewModel,
) {
    var searchResult by remember { mutableStateOf<List<User>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        viewModel.searchUsers(
            query = query,
            onSuccess = { allUsers ->
                val filtered = allUsers.filter { u ->
                    u.id != user.id &&
                    (viewModel.friendsList?.none { it == u.id } ?: true)
                }
                searchResult = filtered
            },

            onFailure = { errorMessage = it.message }
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