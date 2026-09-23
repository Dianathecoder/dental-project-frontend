package com.example.dynalar_frontend_v1.ui.screens.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.management.Treatment
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.CustomisableDynamicDropdownMenu
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScheduleAppointmentPage(
    initialDate: LocalDate = LocalDate.now(),
    initialHour: Int = 9,
    initialMinute: Int = 0,
    patientViewModel: PatientViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    materialViewModel: MaterialViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isPatient = sessionManager.hasRole("PATIENT") || sessionManager.hasRole("ROLE_PATIENT")
    val loggedInUserId = sessionManager.getUserId()

    var selectedDate by remember { mutableStateOf(initialDate) }
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    var selectedTreatment by remember { mutableStateOf<Treatment?>(null) }
    var selectedDoctor by remember { mutableStateOf<User?>(null) }

    val margen = 5
    val totalMinutes = hour * 60 + minute + (selectedTreatment?.durationMinutes ?: 30) + margen
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var description by remember { mutableStateOf("") }

    var showInfectionWarning by remember { mutableStateOf(false) }
    var hasAcceptedInfectionWarning by remember { mutableStateOf(false) }

    LaunchedEffect(isPatient, loggedInUserId) {
        if (isPatient && loggedInUserId != -1L) {
            patientViewModel.getPatientById(loggedInUserId)
        }
    }

    if (isPatient) {
        val patientData = patientViewModel.selectedPatient
        LaunchedEffect(patientData) {
            if (patientData != null && patientData.id == loggedInUserId) {
                selectedPatient = patientData
            }
        }
    }

    LaunchedEffect(selectedTreatment) {
        selectedDoctor = null
    }

    LaunchedEffect(appointmentViewModel.uiStateAutoAssign) {
        if (appointmentViewModel.uiStateAutoAssign is InterfaceGlobal.Success) {
            selectedTreatment?.materials?.forEach { treatmentMaterial ->
                treatmentMaterial.material.id?.let { materialId ->
                    materialViewModel.decreaseStock(
                        id = materialId,
                        quantity = treatmentMaterial.quantityRequired
                    )
                }
            }

            appointmentViewModel.resetAutoAssignState()
            onBackClick()
        }
    }

    if (showInfectionWarning) {
        AlertDialog(
            onDismissRequest = { showInfectionWarning = false },
            title = { Text(stringResource(R.string.appointment_warning_title), fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                val infections = selectedPatient?.medicalRecord?.infectiousDeceases ?: ""
                Text(stringResource(R.string.appointment_warning_msg, infections))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInfectionWarning = false
                        hasAcceptedInfectionWarning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                ) {
                    Text(stringResource(R.string.btn_understood))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Column(modifier = Modifier.padding(24.dp)) {
                val isLoading = appointmentViewModel.uiStateAutoAssign is InterfaceGlobal.Loading
                val canConfirm = selectedPatient != null && selectedTreatment != null && selectedDoctor != null && !isLoading

                Navegate_Button(
                    text = if (isLoading) stringResource(R.string.appointment_assigning) else stringResource(R.string.appointment_confirm_btn),
                    onClick = {
                        if (!canConfirm) return@Navegate_Button

                        val hasInfections = !selectedPatient?.medicalRecord?.infectiousDeceases.isNullOrBlank()

                        if (hasInfections && !hasAcceptedInfectionWarning) {
                            showInfectionWarning = true
                        } else {
                            appointmentViewModel.autoAssign(
                                patientId = selectedPatient!!.id!!,
                                treatmentId = selectedTreatment!!.id!!,
                                doctorId = selectedDoctor!!.id!!,
                                date = selectedDate,
                                hour = hour,
                                minute = minute,
                                reason = description
                            )
                        }
                    },
                    backgroundColor = if (canConfirm) ButtonPrimary else Color.Gray,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            CustomTopBar(title = stringResource(R.string.appointment_new_title), onNavigateBack = onBackClick)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (isPatient && selectedPatient != null) {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Reserva a nom de: ${selectedPatient!!.name} ${selectedPatient!!.lastName}",
                            color = Color(0xFF1A5BB2),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                AppointmentFormContent(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    hour = hour, minute = minute,
                    onStartTimeChange = { h, m -> hour = h; minute = m },
                    endHour = endHour, endMinute = endMinute,
                    onEndTimeChange = { _, _ -> },
                    selectedPatient = selectedPatient,
                    onPatientSelected = if (isPatient) null else { it -> selectedPatient = it },
                    selectedTreatment = selectedTreatment,
                    onTreatmentSelected = { selectedTreatment = it },
                    selectedDoctor = selectedDoctor,
                    onDoctorSelected = { selectedDoctor = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    patientViewModel = patientViewModel,
                    treatmentViewModel = treatmentViewModel,
                    appointmentViewModel = appointmentViewModel,
                    userViewModel = userViewModel
                )
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormContent(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    hour: Int,
    minute: Int,
    onStartTimeChange: (Int, Int) -> Unit,
    endHour: Int,
    endMinute: Int,
    onEndTimeChange: (Int, Int) -> Unit,
    selectedPatient: Patient? = null,
    onPatientSelected: ((Patient) -> Unit)? = null,
    selectedTreatment: Treatment?,
    onTreatmentSelected: (Treatment) -> Unit,
    selectedDoctor: User? = null,
    onDoctorSelected: (User) -> Unit = {},
    description: String,
    onDescriptionChange: (String) -> Unit,
    patientViewModel: PatientViewModel,
    treatmentViewModel: TreatmentViewModel,
    appointmentViewModel: AppointmentViewModel,
    userViewModel: UserViewModel
) {
    var showCalendar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        treatmentViewModel.getTreatments()
        userViewModel.getAllStaff()
        if (onPatientSelected != null) {
            patientViewModel.getPatients()
        }
    }

    LaunchedEffect(selectedTreatment) {
        if (selectedTreatment != null) {
            userViewModel.getDoctorsByTreatment(selectedTreatment.id!!)
        }
    }

    // Consulta de slots pasando id de paciente, tratamiento y doctor seleccionado
    LaunchedEffect(selectedTreatment, selectedDate, selectedPatient, selectedDoctor) {
        val pId = selectedPatient?.id
        val tId = selectedTreatment?.id
        val dId = selectedDoctor?.id

        if (pId != null && tId != null && dId != null) {
            val start = selectedDate.with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            appointmentViewModel.fetchSlots(
                patientId = pId,
                treatmentId = tId,
                startDate = start,
                endDate = end,
                doctorId = dId
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = if (selectedTreatment != null) "%02d:%02d → %02d:%02d".format(hour, minute, endHour, endMinute)
            else "%02d:%02d → --:--".format(hour, minute),
            fontSize = 14.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        SectionLabel(icon = R.drawable.visita_tiempo, text = "Día y hora de la visita")
        Row(verticalAlignment = Alignment.CenterVertically) {
            EditableChip(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, d 'de' MMM", Locale.forLanguageTag("es"))),
                onClick = { showCalendar = true }
            )
            Spacer(Modifier.width(10.dp))
            EditableChip(text = "%02d:%02d".format(hour, minute), onClick = { })
            Spacer(Modifier.width(8.dp))
            Text("→", color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            EditableChip(
                text = if (selectedTreatment != null) "%02d:%02d".format(endHour, endMinute) else "--:--",
                onClick = { }
            )
        }

        if (onPatientSelected != null) {
            Spacer(Modifier.height(24.dp))
            SectionLabel(icon = R.drawable.visita_paciente, text = "Paciente")
            when (val pState = patientViewModel.uiStatePatient) {
                is InterfaceGlobal.Success -> {
                    CustomisableDynamicDropdownMenu(
                        selectedItem = selectedPatient,
                        options = pState.data,
                        label = "Seleccionar pacient",
                        displayText = { "${it.name ?: ""} ${it.lastName ?: ""}".trim() },
                        onItemSelected = onPatientSelected
                    )
                }
                is InterfaceGlobal.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                else -> UnavailableChip("No s'han pogut carregar pacients")
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel(icon = R.drawable.visita_tratamientos, text = "Tratamiento")
        when (val tState = treatmentViewModel.uiStateTreatment) {
            is InterfaceGlobal.Success -> {
                val filteredTreatments = if (selectedPatient != null) {
                    val hasSignedAnesthesia = selectedPatient.anesthesiaConsent == true ||
                            !selectedPatient.medicalRecord?.signatureBase64.isNullOrBlank()

                    if (!hasSignedAnesthesia) {
                        tState.data.filter { treatment ->
                            treatment.materials?.none { it.material.name.contains("Anest", ignoreCase = true) } ?: true
                        }
                    } else {
                        tState.data
                    }
                } else {
                    tState.data
                }

                CustomisableDynamicDropdownMenu(
                    selectedItem = selectedTreatment,
                    options = filteredTreatments,
                    label = "Seleccionar tractament",
                    displayText = { it.name ?: "" },
                    onItemSelected = onTreatmentSelected
                )
            }
            is InterfaceGlobal.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            else -> UnavailableChip("No hi ha tractaments disponibles")
        }

        // --- DESPLEGABLE CON CONTROL Y ETIQUETA VISUAL DE DOCTORES ---
        if (selectedTreatment != null) {
            Spacer(Modifier.height(24.dp))
            SectionLabel(icon = R.drawable.visita_paciente, text = "Doctor")

            val allDoctors = userViewModel.staffList.filter {
                it.roles.any { role -> role.uppercase().contains("DOCTOR") || role.uppercase().contains("DENTIST") }
            }

            val qualifiedDocIds = if (userViewModel.qualifiedDoctors is InterfaceGlobal.Success) {
                (userViewModel.qualifiedDoctors as InterfaceGlobal.Success).data.map { it.id }
            } else {
                emptyList()
            }

            var expandedDoctor by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedDoctor,
                onExpandedChange = { expandedDoctor = !expandedDoctor }
            ) {
                OutlinedTextField(
                    value = selectedDoctor?.let { "Dr/a. ${it.name ?: ""} ${it.surname ?: ""}".trim() } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Doctor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDoctor) },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ButtonPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedDoctor,
                    onDismissRequest = { expandedDoctor = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (allDoctors.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hi ha doctors registrats a la clínica.") }, onClick = {})
                    } else {
                        allDoctors.forEach { doc ->
                            val isQualified = qualifiedDocIds.contains(doc.id)

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Dr/a. ${doc.name ?: ""} ${doc.surname ?: ""}".trim(),
                                            fontSize = 14.sp,
                                            color = if (isQualified) Color.Black else Color.Gray
                                        )
                                        Spacer(Modifier.weight(1f))

                                        Surface(
                                            color = if (isQualified) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (isQualified) "Assignat" else "No Assignat",
                                                color = if (isQualified) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    if (isQualified) {
                                        onDoctorSelected(doc)
                                        expandedDoctor = false
                                    }
                                },
                                enabled = isQualified
                            )
                        }
                    }
                }
            }
        }

        if (selectedTreatment != null && selectedDoctor != null) {
            Spacer(Modifier.height(28.dp))
            SectionLabel(icon = R.drawable.visita_tiempo, text = "Horaris Disponibles")
            when (val slotsState = appointmentViewModel.uiStateSlots) {
                is InterfaceGlobal.Success -> {
                    val todaySlots = slotsState.data[selectedDate.toString()] ?: emptyList()
                    if (todaySlots.isEmpty()) {
                        UnavailableChip("No hi ha forats lliures per a aquest doctor")
                    } else {
                        TimeSlotGrid(todaySlots, hour, minute) { h, m -> onStartTimeChange(h, m) }
                    }
                }
                is InterfaceGlobal.NotFound -> {
                    UnavailableChip("No hi ha forats lliures per a aquest doctor")
                }
                is InterfaceGlobal.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
                is InterfaceGlobal.Error -> {
                    UnavailableChip("Error al consultar la disponibilitat")
                }
                else -> UnavailableChip("Consultant disponibilitat...")
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionLabel(icon = R.drawable.visita_descripcion, text = "Notas")
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Afegeix una descripció...") },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF9F9F9),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }

    if (showCalendar) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateChange(java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.of("UTC")).toLocalDate())
                    }
                    showCalendar = false
                }) { Text("Acceptar", color = ButtonPrimary) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun SectionLabel(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ButtonPrimary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun EditableChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFE8EEF1),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(44.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = text, fontSize = 14.sp, color = ButtonPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun UnavailableChip(text: String) {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = text, fontSize = 14.sp, color = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeSlotGrid(slots: List<String>, selH: Int, selM: Int, onSelect: (Int, Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        slots.forEach { time ->
            val parts = time.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val isSelected = selH == h && selM == m

            Surface(
                onClick = { onSelect(h, m) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) ButtonPrimary else Color(0xFFF0F4F8),
                modifier = Modifier.width(74.dp)
            ) {
                Text(
                    text = time,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF455A64)
                )
            }
        }
    }
}