package com.example.fairsplit.composeUi.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.R
import com.example.fairsplit.data.User
import com.example.fairsplit.view.model.GroupViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

import com.example.fairsplit.composeUi.components.UserCard
import com.example.fairsplit.composeUi.dialogs.AddUserDialog

import com.example.fairsplit.view.model.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupScreen(
    currentUser: User?,
    onBack: () -> Unit,
    onAddFriends: () -> Unit,
    gVM: GroupViewModel,
    uVM: UserViewModel
) {
    uVM.loadFriendsUsers()
    gVM.creatorId = currentUser!!.id
    var showDialog by remember { mutableStateOf(false) }
    val snackBarScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

// Registers a photo picker activity launcher in single-select mode.
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            val inputStream = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream).asImageBitmap()
            inputStream?.close()
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    } //Bearbeiteter Code von: https://developer.android.com/training/data-storage/shared/photopicker#compose
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Neue Gruppe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (val result = gVM.validateGroup()) { //Prüfe ob alles mit der Gruppe in Ordnung ist: Name, Bild
                        is GroupViewModel.ValidationResult.Error ->  //Wenn nicht, gib den Grund dafür an
                            snackBarScope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        is GroupViewModel.ValidationResult.Success -> {  //ansontsen erstelle die Gruppe
                            gVM.createGroup(onSuccess = {
                                onBack()
                            })
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                icon = {},
                text = {
                    Text(text = "Gruppe Erstellen")
                }
            )
        }

    ) { innerPadding ->
        Box (Modifier.padding(innerPadding)) {
            if (showDialog) {   //Ist true, wenn AddUser (+ Icon) gedrückt wurde
                AddUserDialog(
                    gVM = gVM,
                    uVM = uVM,
                    showDialog = true,
                    onDismiss = { showDialog = false },
                    onUserSelected = { user: User ->
                        gVM.addUser(user)
                    },
                    onAddFriends = onAddFriends,
                )
            }
            CreateGroupScreen(
                onAddPictureClick = {
                    //photo picker launchen und User kann nur Bilder auswählen
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onAddToGroupClick = { user ->
                    gVM.addUser(user)
                    showDialog = false
                },
                selectedUsers = gVM.selectedUsers,
                onShowDialogChange = { showDialog = it },
                removeUser = { user -> gVM.removeUserWhileCreatingGroup(user) },
                gVM = gVM,
                bitmap = bitmap //Für Bild, wird aber nicht in firebase gespeichert
            )
        }
    }
}

@Composable
fun CreateGroupScreen(
    onAddPictureClick: () -> Unit,
    onAddToGroupClick: (User) -> Unit,
    selectedUsers: List<User>,
    onShowDialogChange: (Boolean) -> Unit,
    removeUser: (User) -> Unit,
    gVM: GroupViewModel,
    bitmap: ImageBitmap?
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.disneyland),
                    contentDescription = "Gruppenbild",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onAddPictureClick,
                modifier = Modifier.height(60.dp)
            ) {
                Text(text = "Bild Auswählen", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = gVM.groupName,
            onValueChange = { gVM.groupName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Gruppennamen einfügen") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Gruppenbeschreibung", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = gVM.groupDescription,
            onValueChange = { gVM.groupDescription = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            placeholder = { Text("Gruppenbeschreibung hinzufügen") },
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "In Gruppe hinzufügen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onShowDialogChange(true) }) {
                Icon(
                    painter = painterResource(id = R.drawable.plus_icon),
                    contentDescription = "Add to group",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (selectedUsers.isEmpty()) {
                Text("Keine Mitglieder hinzugefügt.", color = Color.Gray)
            } else {
                LazyColumn {
                    items(selectedUsers, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            showDeleteIcon = true,
                            onClick = { removeUser(user) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(85.dp))
    }
}

