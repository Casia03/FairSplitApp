package com.example.fairsplit.composeUi.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fairsplit.R
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.fairsplit.view.model.LoginViewModel
import com.example.fairsplit.view.model.UserViewModel

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    lViewModel: LoginViewModel,
    onSignOut: () -> Unit = {},
    onFriendList: () -> Unit = {},
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Profilbild + Bearbeiten-Icon
        Box(modifier = Modifier.size(120.dp)) {
            Image(
                painter = painterResource(id = R.drawable.avatar), // Beispielbild
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center),

            ) //contentScale = ContentScale.Crop
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Bearbeiten",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = viewModel.name, style = MaterialTheme.typography.titleMedium)
        Text(text = viewModel.name_lowercase, color = Color.Gray)
        Text(text = viewModel.email, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Optionen
        ProfileOption(
            icon = Icons.Default.Share,
            label = "Freunde Einladen",
            onClick = { }
//            onClick = onInviteFriends
        )
        ProfileOption(
            icon = Icons.Default.List,
            label = "Freundesliste",
            onClick = {
                onFriendList()
                Log.d("ProfileOption", "Das wurde auch getriggert")
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            onClick = {
                lViewModel.logout(context)
                onSignOut()
            },
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text("Sign out", color = Color.White, fontFamily = FontFamily(Font(R.font.manrope_bold)))
        }
    }
}

@Composable
fun AsyncImage(
    model: String,
    contentDescription: Nothing?,
    modifier: Modifier,

) {
    //TODO("Not yet implemented")
} //contentScale: Crop

@Composable
fun ProfileOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                Log.d("ProfileOption", "Clicked Freundesliste")
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFe9eef2), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}


