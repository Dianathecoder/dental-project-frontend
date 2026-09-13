package com.example.dynalar_frontend_v1.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.patient.Sex
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.screens.patient.InformationPersonal
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserPage(
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")

    val availableRoles = remember(isSuperAdmin, isOwner) {
        when {
            isSuperAdmin -> listOf(
                "AUXILIAR" to "Auxiliar",
                "DOCTOR" to "Doctor/a",
                "ADMIN" to "Administrador",
                "OWNER" to "Propietari"
            )
            isOwner -> listOf(
                "AUXILIAR" to "Auxiliar",
                "DOCTOR" to "Doctor/a",
                "ADMIN" to "Administrador"
            )
            else -> listOf(
                "AUXILIAR" to "Auxiliar",
                "DOCTOR" to "Doctor/a"
            )
        }
    }

    var selectedRole by remember { mutableStateOf(availableRoles.first().first) }
    var expanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var countryCode by remember { mutableStateOf("+34") }
    var phone by remember { mutableStateOf("") }

    val adminInviteState by adminViewModel.inviteState.collectAsState()

    val fillAllFieldsMsg = stringResource(R.string.validation_fill_all_fields)
    val invalidEmailMsg = stringResource(R.string.validation_invalid_email)
    val invalidPhoneMsg = stringResource(R.string.validation_invalid_phone)
    val invalidDniMsg = stringResource(R.string.validation_invalid_dni)
    val userCreatedSuccessMsg = stringResource(R.string.user_created_successfully)
    val unknownServerErrorMsg = stringResource(R.string.error_unknown_server)

    LaunchedEffect(adminInviteState) {
        if (adminInviteState is InterfaceGlobal.Success) {
            Toast.makeText(context, userCreatedSuccessMsg, Toast.LENGTH_SHORT).show()
            adminViewModel.resetInviteState()
            onNavigateBack()
        } else if (adminInviteState is InterfaceGlobal.Error) {
            val errorState = adminInviteState as InterfaceGlobal.Error
            val errorMsg = errorState.message ?: unknownServerErrorMsg
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            adminViewModel.resetInviteState()
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isLoading = adminInviteState is InterfaceGlobal.Loading
                    Navegate_Button(
                        text = stringResource(R.string.btn_create),
                        onClick = {
                            if (name.isBlank() || lastName.isBlank() || email.isBlank() || dni.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, fillAllFieldsMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }
                            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
                            if (!email.matches(emailRegex)) {
                                Toast.makeText(context, invalidEmailMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }
                            val phoneRegex = "^[0-9]{9}$".toRegex()
                            if (!phone.trim().matches(phoneRegex)) {
                                Toast.makeText(context, invalidPhoneMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }
                            val dniRegex = "^[XYZxyz]?\\d{7,8}[A-Za-z]$".toRegex()
                            if (!dni.trim().matches(dniRegex)) {
                                Toast.makeText(context, invalidDniMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            adminViewModel.inviteUser(name, lastName, email, selectedRole, dni, "$countryCode $phone", sex.name)
                        },
                        isLoading = isLoading,
                        enabled = !isLoading
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            CustomTopBar(
                title = "Nou Treballador/a",
                titleFontSize = 20.sp,
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = availableRoles.find { it.first == selectedRole }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol del treballador/a") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF537895),
                            focusedLabelColor = Color(0xFF537895)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableRoles.forEach { (roleKey, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName) },
                                onClick = {
                                    selectedRole = roleKey
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                InformationPersonal(
                    name = name, onNameChange = { name = it },
                    lastName = lastName, onLastNameChange = { lastName = it },
                    email = email, onEmailChange = { email = it },
                    dni = dni, onDniChange = { dni = it },
                    countryCode = countryCode, onCountryCodeChange = { countryCode = it },
                    phone = phone, onPhoneChange = { phone = it },
                    sex = sex, onSexChange = { sex = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}