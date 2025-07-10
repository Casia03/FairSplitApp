package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.data.User
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.SpendingViewModel
import com.example.fairsplit.view.model.SpendingViewModel.UserModel
import com.example.fairsplit.view.model.SpendingViewModel.ValidationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Screen to add a new spending entry within a group.
 *
 * Loads and initializes the spending data on first composition.
 * Displays input fields for spending title and amount with validation for the amount input.
 * Shows UI to select the split mode (evenly or individual) and assign the creditor (payer).
 * Uses [CostSplitScreen] to display and edit individual user contributions based on the selected split mode.
 *
 * Args:
 * - activeUser: Currently logged-in user who is adding the spending.
 * - sVM: SpendingViewModel handling spending data and logic.
 * - gVM: GroupViewModel, used for group-related data.
 * - onBack: Callback invoked when navigating back.
 * - groupId: Identifier of the group where the spending will be added.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpendingScreen(
    activeUser: User?,
    sVM: SpendingViewModel,
    gVM: GroupViewModel,
    onBack: () -> Unit,
    groupId: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Lade die Daten beim ersten Composable-Aufruf
    LaunchedEffect(Unit) {
        sVM.initAddSpending(activeUser = activeUser, groupId = groupId)
    }
    val labelTextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    val placeholderTextStyle = TextStyle(
        fontSize = 16.sp,
        color = Color.Gray
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neue Ausgabe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Bezeichnung", style = labelTextStyle)
            OutlinedTextField(
                value = sVM.spendingTitle,
                onValueChange = { sVM.updateTitle(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                placeholder = { Text("Bezeichnung", style = placeholderTextStyle) },
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sVM.amountString,
                    onValueChange = { newValue ->
                        val cleaned = newValue.filter { it.isDigit() || it == ',' }
                        // Nur ein Komma zulassen
                        val parts = cleaned.split(',')
                        val corrected = when {
                            parts.size > 2 -> sVM.amountString // Mehrere Kommas? Ignorieren
                            parts.size == 2 && parts[1].length > 2 -> sVM.amountString // Zuviel Nachkommastellen? Ignorieren
                            else -> cleaned
                        }
                        sVM.amountString = corrected
                        sVM.updateAmount(corrected)
                    },
                    modifier = Modifier
                        .width(90.dp)
                        .height(55.dp),
                    placeholder = { Text("0,00", style = placeholderTextStyle) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "Euro", fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Aufteilung",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bezahlt von", Modifier
                        .clip(RoundedCornerShape(35))
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .background(Color.Green)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(53.dp))
                SplitModeSelector(
                    splitMode = sVM.splitMode,
                    onSplitModeChange = sVM::updateSplitMode
                )
            }
            CostSplitScreen(
                splitMode = sVM.splitMode,
                people = sVM.members,
                onCreditorChanged = { index, _ -> sVM.changeCreditor(index) },
                sVM = sVM,
                onBack = onBack,
                scope = scope,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

enum class SplitMode {
    EVENLY, INDIVIDUAL
}

/**
 * UI component to select the cost splitting mode.
 *
 * Displays two chips for choosing between "Gleichmäßig" (evenly) and "Individuell" (individual) split modes.
 * Highlights the currently selected mode and calls back when the user changes the selection.
 *
 * Args:
 * - splitMode: Currently selected split mode.
 * - onSplitModeChange: Callback invoked when the split mode changes.
 */
@Composable
fun SplitModeSelector(
    splitMode: SplitMode,
    onSplitModeChange: (SplitMode) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = splitMode == SplitMode.EVENLY,
            onClick = { onSplitModeChange(SplitMode.EVENLY) },
            label = { Text("Gleichmäßig") }
        )
        FilterChip(
            selected = splitMode == SplitMode.INDIVIDUAL,
            onClick = { onSplitModeChange(SplitMode.INDIVIDUAL) },
            label = { Text("Individuell") }
        )
    }
}

@Composable
fun CostSplitScreen(
    splitMode: SplitMode,
    people: List<UserModel>,
    onCreditorChanged: (index: Int, UserModel) -> Unit,
    sVM: SpendingViewModel,
    onBack: () -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    val scope = scope
    val snackbarHostState = snackbarHostState
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(people) { index, person ->
                    PersonRow(
                        userModel = person,
                        isEditable = splitMode == SplitMode.INDIVIDUAL,
                        onAmountChange = { newAmount ->
                            sVM.updateIndividualAmount(index, newAmount)
                        },
                        onClick = {
                            // Toggle Auswahlstatus:
                            if(person.id != sVM.creditorId) { //Creditor darf nicht selbst abgewählt sein
                                sVM.toggleUser(index)
                            }
                        },
                        onIconClicked = {
                            onCreditorChanged(
                                index,
                                person.copy(hasGreenCircle = !person.hasGreenCircle)
                            )
                        }
                    )
                }
            }

            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = Color.Gray,
                thickness = 1.dp
            )

            Text(
                "Übrig: ${"%.2f".format(sVM.difference).replace('.', ',')} Euro",
                fontWeight = FontWeight.Bold
            )


            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Button(
                onClick = {
                    when (val result = sVM.validateSpending()) {
                        is ValidationResult.Success -> {
                            sVM.createRegularSpendings(onSuccess = {
                                onBack() })
                        }

                        is ValidationResult.Error -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Bestätigen") }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
fun PersonRow(
    userModel: UserModel,
    isEditable: Boolean,
    onAmountChange: (String) -> Unit,
    onClick: () -> Unit,
    onIconClicked: () -> Unit,
    ) {
    val bgColor = if (!userModel.isSelected) Color(0xFFAAAAAA) else Color.Transparent
    val textColor = if (!userModel.isSelected) Color.White else Color.Black
    val borderModifier = if (userModel.hasGreenCircle) Modifier.border(5.dp, Color.Green, CircleShape) else Modifier
    Row(
        Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        ) {
        Box(
            modifier = Modifier
                .then(
                    if (userModel.hasGreenCircle) Modifier
                        .border(3.dp, Color.Green, CircleShape)
                    else Modifier
                )
                .padding(8.dp) // Etwas Abstand, damit der grüne Kreis nicht "klebt"
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = userModel.icon),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .clickable { onIconClicked() },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(userModel.name, color = textColor, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = userModel.amountString,
            onValueChange = onAmountChange,
            modifier = Modifier
                .width(70.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(20))
                .background(Color(0xFF999999)),
            enabled = isEditable && userModel.isSelected,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
            placeholder = { Text("0,00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
