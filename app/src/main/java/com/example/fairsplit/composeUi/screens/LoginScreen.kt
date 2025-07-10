package com.example.fairsplit.composeUi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairsplit.ui.theme.background
import com.example.fairsplit.ui.theme.iconColor
import com.example.fairsplit.ui.theme.tertiaryBlue
import com.example.fairsplit.view.model.LoginViewModel
import kotlinx.coroutines.launch

@Composable
    fun LoginScreen(
    logVM: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        var errorMessage by remember { mutableStateOf<String?>(null) }

        val keyboardController = LocalSoftwareKeyboardController.current

        val context = LocalContext.current

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        var passwordVisible by remember { mutableStateOf(false) }

        var loading by remember { mutableStateOf(false) }

        val textStyle =
            androidx.compose.ui.text.TextStyle(                         // TextStyle, texteinstellungen fur die Seite
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        var passCriteria = androidx.compose.ui.text.TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFffd1d1)
        )

        // Um HEX Farben einzugeben ist 0xFF am Anfang nötig, 0x fur Hex FF fur alpha rest fur Farbe
        val textFieldCustomColor = Color(0xFFe3d6ff)

        val modifyColors = TextFieldDefaults.colors(
            //Field Background
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.LightGray,

            // Fuer den Underline, damit es Verschwindet
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,

            focusedPlaceholderColor = Color.LightGray,
            unfocusedPlaceholderColor = Color.LightGray,
            disabledPlaceholderColor = Color.LightGray,

            errorIndicatorColor = Color.Transparent,            // Unterstrich soger bei IsError Transparent machen

        )
        val textFieldCustomModifiers =
            Modifier                                     //TextField Modifikatoren
                .padding(10.dp)
                .background(Color.Transparent)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = background,
        modifier = Modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(background)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .background(tertiaryBlue, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
                    style = textStyle
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = textFieldCustomModifiers,
                    colors = modifyColors,
                    shape = RoundedCornerShape(8.dp)
                )
                Text(
                    text = "Password",
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
                    style = textStyle
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Passwort verbergen" else "Passwort anzeigen")
                        }
                    },
                    modifier = textFieldCustomModifiers,
                    colors = modifyColors,
                    shape = RoundedCornerShape(8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onRegisterClick() },
                        colors = ButtonDefaults.buttonColors(iconColor)
                    ) {
                        Text(text = "Register",
                            color = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            // tastatur verstecken
                            keyboardController?.hide()

                            loading = true
                            logVM.loginUser(
                                context,
                                email,
                                password,
                                onSuccess = { user ->
                                    loading = false
                                    onLoginSuccess()
                                },
                                onFailure = { error ->
                                    loading = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(error.message ?: "Login fehlgeschlagen")
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(iconColor)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Login",
                                color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

