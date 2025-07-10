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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.fairsplit.R
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.fairsplit.data.GroupPreview
import com.example.fairsplit.data.Spending
import com.example.fairsplit.view.model.GroupViewModel
import androidx.compose.material3.Scaffold
import com.example.fairsplit.ui.theme.creditGreen
import com.example.fairsplit.ui.theme.debtRed

@Composable
fun GroupScreen(
    viewModel: GroupViewModel,
    onAddGroupClick: () -> Unit,
    onGroupDetailsClick: (String) -> Unit,
    onAddSpendingClick: () -> Unit,
) {

    val groupPreview = remember { mutableStateListOf<GroupPreview>() }
    val balance = viewModel.totalBalance
    var errorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {

        viewModel.loadTotalBalance()
        viewModel.getGroupPreviewsForUser(
            onSuccess = { groups ->
                groupPreview.clear()
                groupPreview.addAll(groups)
                viewModel.loadTotalBalance()
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Fehler beim Laden"
            }
        )
    }
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues) // WICHTIG: damit der Content nicht vom FAB oder anderen UI-Elementen verdeckt wird
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoBox(
                        text = "${stringResource(R.string.overview_crecit)} ",
                        amount = viewModel.centToEuro(balance?.first ?: 0L),
                        modifier = Modifier.weight(1f),
                        color = creditGreen
                    )
                    InfoBox(
                        text = "${stringResource(R.string.overview_dept)} ",
                        amount = viewModel.centToEuro(balance?.second ?: 0L),
                        modifier = Modifier.weight(1f),
                        color = debtRed
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Deine Gruppen",
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.manrope_bold)),
                        color = colorResource(id = R.color.black)
                    )
                    IconButton(onClick = onAddGroupClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Scrollbare Liste mit Gewichtung
                GroupList(
                    viewModel = viewModel,
                    groups = groupPreview,
                    modifier = Modifier.fillMaxWidth(1f),
                    onGroupDetailsClick = { id -> onGroupDetailsClick(id) }
                )
            }
        }
    )
}

@Composable
fun GroupList(
    viewModel: GroupViewModel,
    groups: List<GroupPreview>,
    modifier: Modifier = Modifier,
    onGroupDetailsClick: (String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(groups.sortedBy { it.name.lowercase() }, key = { it.id }) { group ->
            GroupCard(
                viewModel = viewModel,
                group = group,
                onGroupDetailsClick = { onGroupDetailsClick(group.id) }
            )
        }
    }
}


@Composable
fun GroupCard(
    viewModel: GroupViewModel,
    group: GroupPreview,
    onGroupDetailsClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onGroupDetailsClick() },
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = group.picture),
                contentDescription = "Gruppenbild",
                modifier = Modifier
                    .width(106.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = group.name,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.manrope_extrabold)),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FormattedSpendingText(
    spending: Spending,
    viewModel: GroupViewModel,
    modifier: Modifier = Modifier,
) {
    var formattedText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(spending) {
        viewModel.formatSpending(
            spending,
            onFormatted = { result -> formattedText = result },
            onError = { error -> formattedText = "Fehler" }
        )
    }

    Text(
        text = formattedText ?: "Lädt...",
        modifier = modifier,
        fontSize = 13.sp,
        fontFamily = FontFamily(Font(R.font.manrope_regular)),
        color = Color(0xFF007AFF)
    )
}


@Composable
fun InfoBox(text: String, amount: String, modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .height(120.dp)
            .padding(4.dp)
            .border(
                width = 2.dp,
                color = colorResource(id = R.color.dept_credit_border),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = text,
                modifier = Modifier.padding(start = 16.dp),
                fontFamily = FontFamily(Font(R.font.manrope_bold)),
                fontSize = 16.sp,
                color = colorResource(id = R.color.black)
            )
            Text(
                text = amount,
                modifier = Modifier.padding(start = 16.dp),
                fontFamily = FontFamily(Font(R.font.manrope_bold)),
                fontSize = 16.sp,
                color = color
            )
        }
    }
}

