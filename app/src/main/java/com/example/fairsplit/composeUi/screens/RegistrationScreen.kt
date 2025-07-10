    package com.example.fairsplit.composeUi.screens

    import androidx.compose.animation.animateColorAsState
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
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.statusBars
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextField
    import androidx.compose.material3.TextFieldDefaults
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.example.fairsplit.view.model.RegistrationViewModel
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Visibility
    import androidx.compose.material.icons.filled.VisibilityOff
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.SnackbarHost
    import androidx.compose.material3.SnackbarHostState
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.ui.platform.LocalSoftwareKeyboardController
    import androidx.compose.ui.text.TextStyle
    import com.example.fairsplit.ui.theme.background
    import com.example.fairsplit.ui.theme.iconColor
    import com.example.fairsplit.ui.theme.tertiaryBlue
    import kotlinx.coroutines.launch

    @Composable
    fun RegistrationScreen(
        regVM: RegistrationViewModel,
        onRegistrationSuccess: () -> Unit
    ) {

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        var loading by remember { mutableStateOf(false) }

        val keyboardController = LocalSoftwareKeyboardController.current

        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var repeatedPassword by remember { mutableStateOf("") }
        var strongPassword by remember { mutableStateOf(false) }
        var identicalPassword by remember { mutableStateOf(false) }

        var passwordVisible by remember { mutableStateOf(false) }
        var repeatedPasswordVisible by remember { mutableStateOf(false) }

        val isFormValid = name.isNotBlank() && email.isNotBlank() && strongPassword && identicalPassword

        val passwordStrengthColor by animateColorAsState(
            if (strongPassword) Color.Green else Color(0xFF8a0000),
            label = "PasswordStrengthColor"
        )

        val passwordMatchColor by animateColorAsState(
            targetValue = if (identicalPassword) Color.Green else Color(0xFF8a0000),
            label = "PasswordMatchColor"
        )

        val textStyle =
            TextStyle(                     // TextStyle, texteinstellungen fur die Seite
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        val passCriteria =
            TextStyle(                      // Text fur Passwort suggestions/info
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        val modifyColors = TextFieldDefaults.colors(
            // Farben fur Textfields!!
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

            errorIndicatorColor = Color.Transparent,                // Unterstrich soger bei IsError Transparent machen

        )

        val textFieldCustomModifiers =
            Modifier                                                //TextField Modifikatoren
                .padding(10.dp)
                .background(Color.Transparent)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = background,
        modifier = Modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) { paddingValues ->
        Box(                                                        // Inhaltsbox, beinhaltet alle input Felder und buttons
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(paddingValues)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp) // Dieser wirkt sich auf den Parent (Box)
                    .background(color = tertiaryBlue, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp) // Dieser wirkt sich auf die Inhalte des Columns selbst

            ) {

                // !!! Inhalt des Registrations Box/Column

                Text(
                    text = "Username",
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
                    style = textStyle
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Benutzername Eingeben") },
                    modifier = textFieldCustomModifiers,
                    colors = modifyColors,
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
                    style = textStyle
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Eingeben") },
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
                    onValueChange = {
                        password = it
                        strongPassword = regVM.passwordEvaluator(it).first
                        identicalPassword = repeatedPassword == it
                    },
                    label = { Text("Passwort Eingeben") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) "Passwort verbergen" else "Passwort anzeigen"
                            )
                        }
                    },
                    modifier = textFieldCustomModifiers,
                    colors = modifyColors,
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier                                                             //Custom Underline für passowrd inputfelds
                        .padding(horizontal = 3.dp)
                        .fillMaxWidth(fraction = 0.85f)
                        .height(12.dp)
                        .background(
                            passwordStrengthColor, shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center,

                    ) {
                    Text(text = regVM.passwordEvaluator(password).second, style = passCriteria)
                }

                TextField(
                    value = repeatedPassword,
                    onValueChange = {
                        repeatedPassword = it
                        identicalPassword = password == it
                    },
                    label = { Text("Passwort Wiederholen") },
                    visualTransformation = if (repeatedPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (repeatedPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                        IconButton(onClick = {
                            repeatedPasswordVisible = !repeatedPasswordVisible
                        }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (repeatedPasswordVisible) "Passwort verbergen" else "Passwort anzeigen"
                            )
                        }
                    },
                    isError = !identicalPassword,
                    modifier = textFieldCustomModifiers,
                    colors = modifyColors,
                    shape = RoundedCornerShape(8.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .fillMaxWidth(fraction = 0.85f)
                        .height(12.dp)
                        .background(
                            passwordMatchColor, shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center,
                    ) {
                    Text(
                        text = if (!identicalPassword) "Password stimm nicht überein" else "Password stimmt überein",
                        style = passCriteria
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            // Hide the keyboard
                            keyboardController?.hide()
                            onRegistrationSuccess()
                        },
//                        modifier = Modifier
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified)
                    ) {
                        Text(text = "Return")
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Pushes the next button to the far right

                    Button(
                        onClick = {
                            // Hide the keyboard
                            keyboardController?.hide()

                            loading = true
                            regVM.registerUser(
                                name,
                                email,
                                password,
                                onSuccess = { user ->
                                    loading = false
                                    onRegistrationSuccess()
                                },
                                onFailure = { error ->
                                    loading = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(error.message ?: "Login fehlgeschlagen")
                                    }
                                }
                            ) // Create User
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .padding(start = 30.dp),
                        colors = ButtonDefaults.buttonColors(iconColor)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Register")
                        }
                    }
                }
            }
        }
    }
}