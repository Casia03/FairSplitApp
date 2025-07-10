package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    message: String
){
    Dialog(onDismissRequest = {onCancel()}) {
        Card(modifier = Modifier.width(800.dp)){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = message,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row{
                    Button(
                        onClick = {onConfirm()}
                    ) {
                        Text("Ja")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {onCancel()}
                    ) {
                        Text("Nein")
                    }
                }
            }
        }
    }
}

