package com.example.dynalar_frontend_v1.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AuthViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onAuxiliarLoginSuccess: () -> Unit = {},
    onDentistLoginSuccess: () -> Unit = {},
    onAdminLoginSuccess: () -> Unit = {},
    onPatientLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {}
) {
    val webClientId = stringResource(R.string.web_client_id)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showWelcomeDialog by remember { mutableStateOf(false) }
    var welcomeUser by remember { mutableStateOf<AuthResponse?>(null) }

    val loginUiState by viewModel.userUiState.collectAsState()
    val authUiState by authViewModel.authUiState.collectAsState()
    val sessionManager = remember { SessionManager(context) }

    fun navigateByRoles(roles: Any?) {
        val rolesList = when (roles) {
            is Collection<*> -> roles.map { it.toString().uppercase() }
            else -> emptyList()
        }
        when {
            rolesList.any { it.contains("ADMIN") } -> onAdminLoginSuccess()
            rolesList.any { it.contains("AUXILIAR") } -> onAuxiliarLoginSuccess()
            rolesList.any { it.contains("DOCTOR") || it.contains("DENTIST") } -> onDentistLoginSuccess()
            else -> onPatientLoginSuccess()
        }
    }

    LaunchedEffect(loginUiState) {
        if (loginUiState is InterfaceGlobal.Success) {
            val response = (loginUiState as InterfaceGlobal.Success<AuthResponse>).data

            // GUARDAR TOKEN Y ROLES EN LA SESIÓN
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserRoles(response.roles) // AÑADIR ESTA LÍNEA

            navigateByRoles(response.roles)
        }
    }

    LaunchedEffect(authUiState) {
        if (authUiState is InterfaceGlobal.Success) {
            val response = (authUiState as InterfaceGlobal.Success<AuthResponse>).data

            // GUARDAR TOKEN Y ROLES EN LA SESIÓN
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserRoles(response.roles) // AÑADIR ESTA LÍNEA

            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val isFirstLogin = prefs.getBoolean("first_google_login_${response.userId}", true)

            if (isFirstLogin) {
                prefs.edit().putBoolean("first_google_login_${response.userId}", false).apply()
                welcomeUser = response
                showWelcomeDialog = true
            } else {
                authViewModel.resetState()
                navigateByRoles(response.roles)
            }
        }
    }

    if (showWelcomeDialog && welcomeUser != null) {
        WelcomeGoogleDialog(
            name = welcomeUser!!.name,
            onConfirm = {
                val roles = welcomeUser?.roles
                showWelcomeDialog = false
                authViewModel.resetState()
                navigateByRoles(roles)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Formulario y contenido principal (se renderiza primero)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Image(
                painter = painterResource(id = R.drawable.general_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.login_email_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(id = R.string.login_email_placeholder), color = Color.LightGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF537895)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.login_password_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(id = R.string.login_password_placeholder), color = Color.LightGray) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF537895)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (loginUiState is InterfaceGlobal.Error) {
                val errorState = loginUiState as InterfaceGlobal.Error
                val errorText = when {
                    errorState.stringResId != null -> stringResource(id = errorState.stringResId)
                    else -> errorState.message ?: stringResource(id = R.string.error_generic)
                }
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (authUiState is InterfaceGlobal.Error) {
                val authErrorState = authUiState as InterfaceGlobal.Error
                val authErrorText = when {
                    authErrorState.stringResId != null -> stringResource(id = authErrorState.stringResId)
                    else -> authErrorState.message ?: stringResource(id = R.string.error_msg_format, "Google")
                }
                Text(
                    text = authErrorText,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onForgotPasswordClick) {
                    Text(text = stringResource(id = R.string.login_forgot_password), color = Color(0xFFADD4D9), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

                    when {
                        trimmedEmail.isEmpty() || trimmedPassword.isEmpty() -> {
                            viewModel.setLocalError(R.string.error_fields_empty)
                        }
                        !trimmedEmail.matches(emailRegex) -> {
                            viewModel.setLocalError(R.string.error_invalid_email_format)
                        }
                        trimmedPassword.length < 6 -> {
                            viewModel.setLocalError(R.string.error_password_length)
                        }
                        else -> {
                            viewModel.login(trimmedEmail, trimmedPassword)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF537895))
            ) {
                if (loginUiState is InterfaceGlobal.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = stringResource(id = R.string.login_button), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onRegisterClick) {
                Text(text = stringResource(id = R.string.login_register_prompt), color = Color(0xFF537895), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text(stringResource(id = R.string.login_or_continue), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        googleSignIn(
                            context = context,
                            webClientId = webClientId,
                            onSuccess = { idToken ->
                                Log.d("GOOGLE_LOGIN", "¡Token de Google recibido! Longitud: ${idToken.length}")

                                authViewModel.googleLogin(idToken)
                            },
                            onError = { error ->
                                Log.e("GOOGLE_LOGIN", "Error en Google SignIn: $error")
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF3C4043)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4285F4))
                    Text("o", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFEA4335))
                    Text("o", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFFBBC05))
                    Text("g", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4285F4))
                    Text("l", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF34A853))
                    Text("e", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFEA4335))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.login_continue_google),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color(0xFF3C4043)
                    )

                    if (authUiState is InterfaceGlobal.Loading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF537895),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 2. Selector de idioma (se coloca al final dentro del Box para renderizarse arriba del todo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            LoginLanguageSelector(onLanguageChange = onLanguageChange)
        }
    }
}

@Composable
fun LoginLanguageSelector(onLanguageChange: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var currentLangCode by remember {
        mutableStateOf(prefs.getString("language", "ca") ?: "ca")
    }

    val languages = listOf("ca" to "CA", "es" to "ES", "en" to "EN")
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Idioma", tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentLangCode.uppercase(),
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            name,
                            fontWeight = FontWeight.Medium,
                            color = if (code == currentLangCode) Color(0xFF537895) else Color.Black
                        )
                    },
                    onClick = {
                        expanded = false
                        currentLangCode = code
                        onLanguageChange(code)
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeGoogleDialog(name: String, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(id = R.string.login_welcome_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.login_welcome_greeting, name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF537895),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.login_welcome_msg),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF537895))
            ) {
                Text(stringResource(id = R.string.login_start_btn), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    )
}

private suspend fun googleSignIn(
    context: Context,
    webClientId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(request = request, context = context)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        onSuccess(googleIdTokenCredential.idToken)
    } catch (e: GetCredentialException) {
        Log.e("GoogleSignIn", "Error tipo: ${e::class.simpleName}")
        Log.e("GoogleSignIn", "Mensaje: ${e.message}")
        Log.e("GoogleSignIn", "Causa: ${e.cause}")
        onError("Error Google: ${e::class.simpleName}")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error inesperado: ${e::class.simpleName} → ${e.message}")
        onError("Error: ${e::class.simpleName} → ${e.message}")
    }
}