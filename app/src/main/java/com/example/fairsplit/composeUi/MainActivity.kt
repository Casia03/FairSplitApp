package com.example.fairsplit.composeUi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fairsplit.ui.theme.FairSplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prüfe, ob ein Deep Link vorliegt
        val deepLinkUri = intent?.data

        val groupId = deepLinkUri?.lastPathSegment

        setContent {
            FairSplitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    FairSplitApp(deepLinkGroupId = groupId)
                }
            }
        }
    }
}