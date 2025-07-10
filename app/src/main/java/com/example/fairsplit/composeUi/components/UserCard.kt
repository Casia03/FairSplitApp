package com.example.fairsplit.composeUi.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.R
import com.example.fairsplit.data.User

//Legt fest wie ein User in Listen dargestellt werden soll: Profilbild, Name und gegebenenfalls X Icon um ihn zu löschen

@Composable
fun UserCard(user: User, onClick: () -> Unit, showDeleteIcon: Boolean = false) {

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .fillMaxWidth()
            .height(65.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = user.icon),
                contentDescription = "Icon",
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showDeleteIcon) {
                Image(
                    painter = painterResource(id = R.drawable.x_icon),
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 16.dp)
                )
            }
        }
    }
}
