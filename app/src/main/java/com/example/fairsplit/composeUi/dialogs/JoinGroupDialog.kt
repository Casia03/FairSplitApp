package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupDialog(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card (
            modifier = Modifier.width(400.dp)
        ){
            Column (
                modifier = Modifier.padding(16.dp)
            ){
                Text(
                    "Willst du dieser Gruppe beitreten?",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row {
                    Button(onClick = {onYes()},
                        content = { Text("Ja") },
                        modifier = Modifier.width(80.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {onNo()},
                        content = { Text("Nein") },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}

