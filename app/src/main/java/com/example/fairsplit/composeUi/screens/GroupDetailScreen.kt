package com.example.fairsplit.composeUi.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.R
import com.example.fairsplit.composeUi.dialogs.ConfirmDialog
import com.example.fairsplit.composeUi.dialogs.EditGroupDialog
import com.example.fairsplit.composeUi.dialogs.JoinGroupDialog
import com.example.fairsplit.composeUi.dialogs.ShowGroupInvitationLinkDialog
import com.example.fairsplit.data.Group
import com.example.fairsplit.data.Spending
import com.example.fairsplit.data.TransferType
import com.example.fairsplit.ui.theme.background
import com.example.fairsplit.ui.theme.creditGreen
import com.example.fairsplit.ui.theme.debtRed
import com.example.fairsplit.ui.theme.debtCreditBoxBorder
import com.example.fairsplit.ui.theme.iconColor
import com.example.fairsplit.ui.theme.superLightGray
import com.example.fairsplit.view.model.GroupViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    gVM: GroupViewModel,
    onSuccess: (Group) -> Unit,
    onFailure: (Exception) -> Unit,
    onSettleUp: () -> Unit,
    onEditMembers: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resolvePayments = listOf<String>()
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(groupId) {
        gVM.initGroupDetail(
            groupId = groupId,
            onSuccess = { group ->
                onSuccess(group)
            },
            onFailure = { e ->
                onFailure(e)
            }
        )
        gVM.checkIfUserIsMemberOrShouldJoin(groupId)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = gVM.group?.name ?: "",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_bold))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    val expanded = remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded.value = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mehr Optionen")
                    }

                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Gruppe bearbeiten") },
                            onClick = {
                                expanded.value = false
                                gVM.showEditGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mitglieder bearbeiten") },
                            onClick = {
                                expanded.value = false
                                onEditMembers()
                            }
                        )
                        //Gruppe kann nur vom Creator gelöscht werden (Zeig die Option nur dann an)
                        if (gVM.group?.creator == gVM.currentUserId.value) {
                            DropdownMenuItem(
                                text = { Text("Gruppe löschen") },
                                onClick = {
                                    expanded.value = false
                                    gVM.requestConfirmation("Willst du diese Gruppe wirklich löschen?") {
                                        gVM.deleteGroup(
                                            onSuccess = { onBack() },
                                            onFailure = { e ->
                                                showSnackbar(
                                                    e.message ?: "Fehler beim Löschen"
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }
                        //Gruppe kann nur verlassen werden, wenn nicht Creator (Zeig die Option nur dann an)
                        if (gVM.group?.creator != gVM.currentUserId.value) {
                            DropdownMenuItem(
                                text = { Text("Gruppe verlassen") },
                                onClick = {
                                    expanded.value = false
                                    gVM.requestConfirmation("Willst du die Gruppe wirklich verlassen?") {
                                        gVM.leaveGroup(
                                            onSuccess = onBack,
                                            onFailure = { e ->
                                                showSnackbar(
                                                    e.message ?: "Unbekannter Fehler"
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Einladungslink kopieren") },
                            onClick = {
                                expanded.value = false
                                gVM.showGroupInvitationLink = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary, // Hintergrundfarbe des FAB
                contentColor = MaterialTheme.colorScheme.onPrimary, // Farbe von Icon und Text auf dem FAB
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add, // Das Plus-Icon
                        contentDescription = "Ausgabe hinzufügen Icon"
                    )
                },
                text = {
                    Text(text = "Ausgabe Hinzufügen")

                }
            )
        }
    ) { innerPadding ->
        /*
        Gruppenlink Dialog anzeigen
        Text ist nur auf echtem Handy auswählbar, nicht mit der Maus im Emulator: Bekanntes Problem mit SelectionContainer
        Copy to Clipboard Button funktioniert aber bei beiden
        */
        if (gVM.showGroupInvitationLink) {
            ShowGroupInvitationLinkDialog(
                gVM = gVM,
                onDismiss = { gVM.showGroupInvitationLink = false },
                snackbarHostState = snackbarHostState
            )
        }
        if (gVM.showJoinGroupDialog) {
            JoinGroupDialog(
                onYes = {
                    gVM.addMembersToGroup(
                        mutableListOf(gVM.currentUserId.value!!),
                        onSuccess = { gVM.showJoinGroupDialog = false }
                    )
                },
                onNo = { onBack() }
            )
        }
        if (gVM.showEditGroupDialog) {
            EditGroupDialog(
                onDismiss = { gVM.showEditGroupDialog = false },
                onConfirm = { gVM.showEditGroupDialog = false },
                gVM = gVM
            )
        }
        if (gVM.showConfirmDialog) {
            ConfirmDialog(
                message = gVM.confirmDialogMessage,
                onConfirm = { gVM.confirmAction() },
                onCancel = { gVM.cancelAction() }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Bild, Titel, Beschreibung
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.disneyland), // Beispielbild
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(modifier = Modifier.width(24.dp))
//                Spacer(modifier = Modifier.height(16.dp))
                    gVM.group?.description?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = iconColor,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .defaultMinSize(minHeight = 38.dp)
                    ) {
                        if (gVM.transferList.isEmpty()) {
                            Text(
                                text = "All Set...",
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                                color = Color.Gray,
                                modifier = Modifier.weight(4f)
                            )
                        } else {
                            gVM.transferList.forEach { (debtorId, creditorId, amount) ->
                                if(debtorId == gVM.currentUserId.value || creditorId == gVM.currentUserId.value){

                                    val currentUserCreditor = debtorId != gVM.currentUserId.value
                                    val color = if (currentUserCreditor) creditGreen else debtRed
                                    var debtorName: String = gVM.userNameMap[debtorId] ?: "Lade..."
                                    var creditorName: String = gVM.userNameMap[creditorId] ?: "Lade..."
                                    val text =
                                            if (currentUserCreditor)
                                                "${debtorName} ${stringResource(R.string.group_overview_debt)} "
                                            else
                                                "${stringResource(R.string.group_overview_credit)} ${creditorName}"

                                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = text,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(8f)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "${gVM.centToEuro(amount)}",
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily(Font(R.font.manrope_bold)),
                                                color = color,
                                                modifier = Modifier.weight(4f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onSettleUp() },
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settle_debts_Button),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Ausgaben",
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.manrope_extrabold))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(
                items = gVM.filterdSpendingsForCurrentUser.sortedByDescending { it.date },
                key = { it.spendingId }) { spending ->
                SpendingItem(spending, gVM, false) // get Spendings in View Model bekommen
            }
            item {
                Spacer(modifier = Modifier.padding(30.dp))
            }
        }
    }
}


@Composable
fun SpendingItem(spending: Spending, gVM: GroupViewModel,activityList: Boolean) {
    val spendingParticipation = gVM.spendingInvolved(spending.batchId)
    val currentUserIsCreditor = spending.creditor == gVM.currentUserId.value
    val title = remember { mutableStateOf("Lädt...") }

    // UI-Zustände für Namen und Anzeige
    val creditorName = remember { mutableStateOf("") }
    val debtorName = remember { mutableStateOf("") }
    val textCreditOrDebt = remember { mutableStateOf("") }
    val colorCreditOrDebt = remember { mutableStateOf(Color.Gray) }

    val youString = stringResource(R.string.spendingItem1)
    val suffixString = stringResource(R.string.spendingItem2)
    val textSpendingItemCredit = stringResource(R.string.gdsSpendingItemCredit)
    val textSpendingItemDebt = stringResource(R.string.gdsSpendingItemDebt)

    // Datum formatieren
    val dateFormatter = remember { SimpleDateFormat("dd.MM\nyyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(spending.date)

    // Uhrzeit formatieren (abhängig von API Level)
    val timeFormatted = remember(spending.date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val time = spending.date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
            time.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(spending.date)
        }
    }
    LaunchedEffect(spending.creditor) {
        if (activityList) {
        gVM.getGroupNameById(
            groupId = spending.groupID,
            onSuccess = { name -> title.value = name },
            onFailure = { title.value = "Unbekannt" }
        )
        } else {
            title.value = spending.title
        }
        if (currentUserIsCreditor) {
            // Du bist der Gläubiger

            creditorName.value = youString
            textCreditOrDebt.value = textSpendingItemCredit
            colorCreditOrDebt.value = creditGreen

            gVM.getUserNameById(
                userId = spending.debtor,
                onSuccess = { name -> debtorName.value = "$name" },
                onFailure = { debtorName.value = "Unbekannt" }
            )
        } else {
            // Du bist der Schuldner
            gVM.getUserNameById(
                userId = spending.creditor,
                onSuccess = { name -> creditorName.value = "$name $suffixString" },
                onFailure = { creditorName.value = "Unbekannt $suffixString" }
            )

            if (!currentUserIsCreditor) {
                debtorName.value = youString
            } else {
                gVM.getUserNameById(
                    userId = spending.debtor,
                    onSuccess = { name -> debtorName.value = "$name" },
                    onFailure = { debtorName.value = "Unbekannt" }
                )
            }

            textCreditOrDebt.value = textSpendingItemDebt
            colorCreditOrDebt.value = debtRed
        }
    }
    Card(
        modifier = Modifier
            .padding(horizontal = 0.dp, vertical = 3.dp)
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        )
    ) {
        /////////////DATUM BLOCK/////////////
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,       // vertikal zentriert
            horizontalArrangement = Arrangement.Center
        ) {
            if(spending.transfer == TransferType.REGULAR) {
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .border(1.5.dp, debtCreditBoxBorder, RoundedCornerShape(8.dp))
                        .background(color = background, RoundedCornerShape(8.dp))
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_extrabold)),
                        textAlign = TextAlign.Center,
                        color = iconColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = timeFormatted + " ${stringResource(R.string.gdsTime)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_regular)),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )

                }
                Spacer(modifier = Modifier.width(12.dp))

                /////////////Titel und Beschreibung/////////////

                Column(modifier = Modifier.weight(6f)) {
                    Text(
                        text = title.value,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${creditorName.value} ${gVM.centToEuro(spending.totalAmount)} ${
                            stringResource(R.string.spendingItem3)
                        } ${spending.title} ${stringResource(R.string.spendingItem4)}",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
                /////////////Betrag/////////////
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,       // vertikale Zentrierung
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    if (spendingParticipation == true) {
                        if (creditorName.value == youString) {
                            Text(
                                text = gVM.centToEuro(spending.totalAmount - spending.individualAmount),
                                color = colorCreditOrDebt.value,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily(Font(R.font.manrope_extrabold))
                            )
                        } else {
                            Text(
                                text = gVM.centToEuro(spending.individualAmount),
                                color = colorCreditOrDebt.value,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily(Font(R.font.manrope_extrabold))
                            )
                        }
                        Text(
                            text = textCreditOrDebt.value,
                            fontSize = 9.sp,
                            fontFamily = FontFamily(Font(R.font.manrope_bold)),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                var settleUpText = ""
                if(!currentUserIsCreditor){
                    settleUpText = "${creditorName.value} ${gVM.centToEuro(spending.totalAmount)} ${stringResource(R.string.settleUpItem1)} ${stringResource(R.string.settleUpItem2)} ${stringResource(R.string.spendingItem4)}"
                } else {
                    settleUpText = "${creditorName.value} ${gVM.centToEuro(spending.totalAmount)} ${stringResource(R.string.settleUpItem1)} ${debtorName.value} ${stringResource(R.string.spendingItem4)}"
                }
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .border(1.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .background(color = superLightGray, RoundedCornerShape(8.dp))
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_extrabold)),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = timeFormatted + " ${stringResource(R.string.gdsTime)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_regular)),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = settleUpText,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}