package com.example.fairsplit.composeUi.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fairsplit.composeUi.dialogs.AddFriendDialogComponent
import com.example.fairsplit.composeUi.components.UserCard
import com.example.fairsplit.view.model.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: UserViewModel,
    onReturn: () -> Unit,
    onBack: () -> Unit
) {
    viewModel.loadFriendsUsers()
    var showAddFriendDialog by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {showAddFriendDialog = true}) {
                Icon(Icons.Default.Add, contentDescription = "Freund hinzufügen")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Freundesliste") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Log.d("Friends", "You are in FriendsScreen")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Deine Freunde",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.friendsUsers ?: emptyList()) { friend ->
                    UserCard(
                        user = friend,
                        showDeleteIcon = true,
                        onClick = { viewModel.removeFriend(friend) }
                    )
                }
            }
            if (showAddFriendDialog) {
                AddFriendDialogComponent(
                    showDialog = showAddFriendDialog,
                    onDismiss = { showAddFriendDialog = false },
                    onUserSelected = { user ->
                        // handle user selected
                        viewModel.addFriend(user)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun FriendItem(friendName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder avatar circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = friendName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
