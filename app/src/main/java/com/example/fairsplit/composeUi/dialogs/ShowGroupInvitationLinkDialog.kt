package com.example.fairsplit.composeUi.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.fairsplit.view.model.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun ShowGroupInvitationLinkDialog(
    gVM: GroupViewModel,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val clipboardManager = LocalClipboardManager.current
    val link =
        "https://fairsplit-dbf6e.web.app/group/${gVM.group?.id}" //Link wird für Gruppe 'generiert'
    //Für Snackbar
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(link))
                            scope.launch {
                                snackbarHostState.showSnackbar("In Zwischenablage gespeichert.")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Link kopieren"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onDismiss) {
                        Text("Schließen")
                    }
                }
            },
            text = {
                //Text ist nur auf echtem Handy auswählbar, nicht mit der Maus im Emulator: Bekanntes Problem mit SelectionContainer
                //CopyButton funktioniert aber bei beiden
                SelectionContainer {
                    Text(link)
                }
            }
        )
    }
}