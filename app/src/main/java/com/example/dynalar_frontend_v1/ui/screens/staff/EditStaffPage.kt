package com.example.dynalar_frontend_v1.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.dynalar_frontend_v1.model.patient.Sex
import com.example.dynalar_frontend_v1.model.staff.dentist.DentistAvailabilityDTO
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.screens.patient.InformationPersonal
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStaffPage(
    staffId: Long,
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // 1. CARGA DE DATOS INICIAL
    LaunchedEffect(staffId) {
        userViewModel.getUserById(staffId)
        treatmentViewModel.getTreatments()
        userViewModel.fetchDentistAvailability(staffId)
    }

    val staff = userViewModel.selectedUser
    val availabilityState = userViewModel.dentistAvailability
    val treatments = treatmentViewModel.treatmentList

    // 2. PRECARGA DE DATOS PERSONALES
    var name by remember(staff) { mutableStateOf(staff?.name ?: "") }
    var lastName by remember(staff) { mutableStateOf(staff?.surname ?: "") }
    var email by remember(staff) { mutableStateOf(staff?.email ?: "") }
    var dni by remember(staff) { mutableStateOf(staff?.dni ?: "") }

    var sex by remember(staff) {
        mutableStateOf(
            try { Sex.valueOf(staff?.sex?.uppercase() ?: "MALE") } catch (e: Exception) { Sex.MALE }
        )
    }

    val phoneParts = (staff?.phone ?: "+34 ").split(" ")
    var countryCode by remember(staff) { mutableStateOf(phoneParts.getOrNull(0) ?: "+34") }
    var phone by remember(staff) { mutableStateOf(phoneParts.getOrNull(1) ?: "") }

    // 3. LÓGICA DE ROLES
    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")

    val availableRoles = remember(isSuperAdmin, isOwner) {
        when {
            isSuperAdmin -> listOf("AUXILIAR" to "Auxiliar", "DOCTOR" to "Doctor/a", "ADMIN" to "Administrador", "OWNER" to "Propietari")
            isOwner -> listOf("AUXILIAR" to "Auxiliar", "DOCTOR" to "Doctor/a", "ADMIN" to "Administrador")
            else -> listOf("AUXILIAR" to "Auxiliar", "DOCTOR" to "Doctor/a")
        }
    }

    var selectedRole by remember(staff) {
        val currentRole = staff?.roles?.firstOrNull()?.toString()?.replace("ROLE_", "")?.uppercase()
        mutableStateOf(currentRole ?: availableRoles.first().first)
    }
    var expanded by remember { mutableStateOf(false) }

    // Permitir tratamientos para Doctor, Admin, SuperAdmin y Owner (Todos excepto Auxiliar)
    val showTreatmentsSection = selectedRole != "AUXILIAR"

    // 4. ESTADO DE TRATAMIENTOS (Sincronizado dinámicamente)
    var selectedTreatmentIds by remember(availabilityState) {
        mutableStateOf(availabilityState?.treatmentIds?.toSet() ?: emptySet())
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
                    Navegate_Button(
                        text = "Guardar Canvis",
                        onClick = {
                            if (name.isBlank() || lastName.isBlank() || email.isBlank() || dni.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, "Omple tots els camps", Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            // A. Guardar Tratamientos si no es Auxiliar
                            if (showTreatmentsSection) {
                                val currentDto = availabilityState ?: DentistAvailabilityDTO()
                                val updatedDto = currentDto.copy(
                                    treatmentIds = selectedTreatmentIds.toList()
                                )
                                userViewModel.updateDentistAvailability(staffId, updatedDto) {}
                            }

                            // B. Guardar Datos Personales y Rol
                            userViewModel.updateStaffUser(
                                userId = staffId,
                                name = name,
                                surname = lastName,
                                email = email,
                                role = selectedRole,
                                dni = dni,
                                phone = "$countryCode $phone",
                                sex = sex.name
                            ) {
                                Toast.makeText(context, "Treballador actualitzat correctament", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
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
            CustomTopBar(title = "Editar Treballador/a", titleFontSize = 20.sp, onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(20.dp))

            // Selector de Rol
            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = availableRoles.find { it.first == selectedRole }?.second ?: selectedRole,
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
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableRoles.forEach { (roleKey, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName) },
                                onClick = { selectedRole = roleKey; expanded = false }
                            )
                        }
                    }
                }
            }

            // Datos Personales
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

            Spacer(modifier = Modifier.height(24.dp))

            // Sección Editable de Tratamientos (Para Doctor, Admin, SuperAdmin u Owner)
            if (showTreatmentsSection) {
                Text(
                    text = "Tractaments Assignats",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (treatments.isEmpty()) {
                    Text(
                        text = "No hi ha tractaments disponibles",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(treatments, key = { it.id ?: 0L }) { treatment ->
                            val treatmentId = treatment.id ?: 0L
                            val isChecked = selectedTreatmentIds.contains(treatmentId)

                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    selectedTreatmentIds = if (isChecked) {
                                        selectedTreatmentIds - treatmentId
                                    } else {
                                        selectedTreatmentIds + treatmentId
                                    }
                                },
                                label = { Text("${treatment.name}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF537895),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}