package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fairsplit.view.model.GroupViewModel

@Composable
fun EditGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    gVM: GroupViewModel,
) {
    var groupName by remember { mutableStateOf(gVM.group?.name ?: "") }
    var groupDescription by remember {  mutableStateOf(gVM.group?.description ?: "")}
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Gruppennamen einfügen") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gruppenbeschreibung", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Gruppenbeschreibung hinzufügen") },
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = {
                            gVM.editGroup(newName = groupName, newDescription = groupDescription)
                            onConfirm()
                        },
                        content = { Text("Bestätigen") }

                    )
                    Spacer(modifier = Modifier.width(50.dp))
                    Button(onClick = { onDismiss() }, content = { Text("Schließen") })
                }
            }
        }
    }
}