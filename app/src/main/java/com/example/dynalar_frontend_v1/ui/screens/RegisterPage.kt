package com.example.dynalar_frontend_v1.ui.screens


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.ui.components.InputFieldEditable
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.viewmodel.AuthViewModel

@Composable
fun RegisterPage(
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authUiState by viewModel.authUiState.collectAsState()

    LaunchedEffect(authUiState) {
        if (authUiState is InterfaceGlobal.Success) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.general_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Crear compte",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Nom — usa el componente InputFieldEditable del proyecto
        InputFieldEditable(
            label = "Nom",
            value = name,
            onValueChange = { name = it },
            placeholder = "El teu nom"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cognoms
        InputFieldEditable(
            label = "Cognoms",
            value = surname,
            onValueChange = { surname = it },
            placeholder = "Els teus cognoms"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        InputFieldEditable(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "correu@exemple.com"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contrasenya — necesita el ojo, usamos OutlinedCard + BasicTextField igual que InputFieldEditable
        PasswordField(
            label = "Contrasenya",
            value = password,
            onValueChange = { password = it },
            placeholder = "Mínim 8 caràcters",
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar contrasenya
        PasswordField(
            label = "Confirmar contrasenya",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Repeteix la contrasenya",
            visible = confirmPasswordVisible,
            onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Error del backend
        if (authUiState is InterfaceGlobal.Error) {
            Text(
                text = (authUiState as InterfaceGlobal.Error).message ?: "Error desconegut",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón principal — usa Navegate_Button igual que el resto del proyecto
        Navegate_Button(
            text = "Registrar-se",
            isLoading = authUiState is InterfaceGlobal.Loading,
            onClick = {
                when {
                    name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank() ->
                        Toast.makeText(context, "Omple tots els camps", Toast.LENGTH_SHORT).show()
                    password != confirmPassword ->
                        Toast.makeText(context, "Les contrasenyes no coincideixen", Toast.LENGTH_SHORT).show()
                    password.length < 8 ->
                        Toast.makeText(context, "La contrasenya ha de tenir mínim 8 caràcters", Toast.LENGTH_SHORT).show()
                    else -> viewModel.register(name.trim(), surname.trim(), email.trim(), password)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace para volver al login
        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Ja tens compte? Inicia sessió",
                color = Color(0xFF537895),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Campo de contrasenya con ojo — mismo estilo que OutlinedCard de InputFieldEditable
@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black.copy(alpha = 0.8f)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SolidColor(Color.LightGray.copy(alpha = 0.4f))),
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 40.dp), // espacio para el icono
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(text = placeholder, color = Color.LightGray)
                            }
                            innerTextField()
                        }
                    }
                )

                // Icono del ojo alineado a la derecha
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}