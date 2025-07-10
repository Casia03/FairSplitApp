package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fairsplit.ui.theme.background
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.SpendingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    groupId: String,
    onBack: () -> Unit,
    gVM: GroupViewModel,
    sVM: SpendingViewModel,
    navController: NavController
) {
    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                title = { Text("Schulden Begleichen") },
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentUser = gVM.currentUserId.value

            val debts = gVM.transferList.filter { it.first == currentUser }

            items(debts) { (debtorId, creditorId, amount) ->
                DebtItem(
                    amount = amount,
                    sVM = sVM,
                    gVM = gVM,
                    groupId = groupId,
                    creditorId = creditorId,
                    navController = navController,
                    debtorId = debtorId)
            }
        }
    }
}


@Composable
fun DebtItem(
    amount: Long,
    sVM: SpendingViewModel,
    gVM: GroupViewModel,
    groupId: String,
    creditorId: String,
    navController: NavController,
    debtorId: String
) {
    val scope = rememberCoroutineScope ()
    val amountDebt = gVM.centToEuro(amount)
    val creditorName = gVM.userNameMap[creditorId] ?: "Lade..."
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, // <— für vertikale Zentrierung
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = amountDebt,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center // <— für horizontale Zentrierung des Textes
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column (
                        modifier = Modifier.weight(2f)
                    ){
                        Button(
                            onClick ={
                                scope.launch {
                                    settleUp(sVM, gVM, creditorId, debtorId, amount, groupId)
                                    navController.popBackStack()
                                }
                            }
                            ,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A6F92)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Überwiesen", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    settleUp(sVM, gVM, creditorId, debtorId, amount, groupId)
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A6F92)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bar bezahlt", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "an $creditorName bezahlen",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

fun settleUp (sVM: SpendingViewModel,
              gVM: GroupViewModel,
              creditorId: String,
              debtorId: String,
              amount: Long,
              groupId: String
) {
    sVM.createSettleUpSpending(debtorId,amount, groupId, creditorId)
}
