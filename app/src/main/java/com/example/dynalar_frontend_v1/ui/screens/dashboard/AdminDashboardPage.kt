package com.example.dynalar_frontend_v1.ui.screens.dashboard



import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.ui.components.CardMenuButton
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.AdminViewModel

@Composable
fun AdminDashboardPage(
    adminViewModel: AdminViewModel = viewModel(),
    onNavigatePatients: () -> Unit,
    onNavigateManagement: () -> Unit,
    onNavigateAttendance: () -> Unit, // Para ver los fichajes
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showInviteDialog by remember { mutableStateOf(false) }

    val inviteState by adminViewModel.inviteState.collectAsState()

    LaunchedEffect(inviteState) {
        if (inviteState is InterfaceGlobal.Success) {
            Toast.makeText(context, (inviteState as InterfaceGlobal.Success<String>).data, Toast.LENGTH_SHORT).show()
            showInviteDialog = false
            adminViewModel.resetInviteState()
        } else if (inviteState is InterfaceGlobal.Error) {
            Toast.makeText(context, (inviteState as InterfaceGlobal.Error).message, Toast.LENGTH_LONG).show()
            adminViewModel.resetInviteState()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(title = "Panell de Direcció", onNavigateBack = onLogout)
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Benvingut/da, Administrador", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            Spacer(modifier = Modifier.height(32.dp))

            // MENÚ PRINCIPAL DEL ADMIN
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CardMenuButton(
                    icon = Icons.Default.GroupAdd,
                    title = "Convidar Personal",
                    onClick = { showInviteDialog = true },
                    modifier = Modifier.weight(1f)
                )
                CardMenuButton(
                    icon = Icons.Default.People,
                    title = "Gestió Pacients",
                    onClick = onNavigatePatients,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CardMenuButton(
                    icon = Icons.Default.Inventory,
                    title = "Gestió Clínica",
                    onClick = onNavigateManagement,
                    modifier = Modifier.weight(1f)
                )
                CardMenuButton(
                    icon = Icons.Default.AccessTime,
                    title = "Control Fitxatges",
                    onClick = onNavigateAttendance,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showInviteDialog) {
        InviteWorkerDialog(
            isLoading = inviteState is InterfaceGlobal.Loading,
            onDismiss = { showInviteDialog = false },
            onConfirm = { name, surname, email, role ->
                adminViewModel.inviteUser(name, surname, email, role,dni = "",
                    phone = "",
                    sex = "OTHER")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteWorkerDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Lista de roles que el Admin puede crear
    val roles = listOf("AUXILIAR" to "Auxiliar", "DENTIST" to "Doctor/a")
    var selectedRole by remember { mutableStateOf(roles[0].first) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Donar d'alta personal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = surname, onValueChange = { surname = it },
                    label = { Text("Cognoms") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email (Rebrà l'accés)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                // Dropdown para elegir el rol
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = roles.find { it.first == selectedRole }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEach { (roleKey, roleName) ->
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, surname, email, selectedRole) },
                enabled = name.isNotBlank() && email.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar Invitació")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·lar", color = Color.Gray) }
        }
    )
}