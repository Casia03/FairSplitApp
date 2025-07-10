package com.example.fairsplit.data

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.R
import com.example.fairsplit.ui.theme.FairSplitTheme


data class ActivityEntry(
    val title: String,
    val groupName: String,
    val amount: Double,
    val dateTime: String
)


@Composable
fun ActivityItem(entry: ActivityEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon/Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF3B82F6), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar), // Beispielbild
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Textinformationen
        Column {
            // Erste Zeile mit fett / farbigem Text
            Text(
                buildAnnotatedString {
                    append("Du hast für ")
                    withStyle(SpanStyle(color = Color(0xFF10B981))) {
                        append("${entry.amount.toInt()} Euro ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${entry.title}")
                    }
                    append(" in ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(entry.groupName)
                    }
                    append(" hinzugefügt")
                }
            )
            Text(
                text = entry.dateTime,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
