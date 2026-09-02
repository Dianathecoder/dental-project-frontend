package com.example.dynalar_frontend_v1.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.dynalar_frontend_v1.ui.components.AppointmentFormContent
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import java.time.LocalDate

@Composable
fun ScheduleAppointmentPage(
    initialDate: LocalDate = LocalDate.now(),
    initialHour: Int = 9,
    initialMinute: Int = 0,
    patientViewModel: PatientViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
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
    val margen = 5
    val totalMinutes = hour * 60 + minute + (selectedTreatment?.durationMinutes ?: 30) + margen
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var description by remember { mutableStateOf("") }

    var showInfectionWarning by remember { mutableStateOf(false) }
    var hasAcceptedInfectionWarning by remember { mutableStateOf(false) }

    // --- MAGIA PARA EL PACIENTE ---
    // 1. Si es paciente, lanzamos la petición para cargar sus datos en 2º plano
    LaunchedEffect(isPatient, loggedInUserId) {
        if (isPatient && loggedInUserId != -1L) {
            patientViewModel.getPatientById(loggedInUserId)
        }
    }

    // 2. Observamos la respuesta y lo auto-asignamos
    if (isPatient) {
        val patientData = patientViewModel.selectedPatient
        LaunchedEffect(patientData) {
            if (patientData != null && patientData.id == loggedInUserId) {
                selectedPatient = patientData
            }
        }
    }

    // Éxito al crear
    LaunchedEffect(appointmentViewModel.uiStateAutoAssign) {
        if (appointmentViewModel.uiStateAutoAssign is InterfaceGlobal.Success) {
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
                val canConfirm = selectedPatient != null && selectedTreatment != null && !isLoading

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

                // 3. CARTEL VISUAL PARA EL PACIENTE
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
                    onEndTimeChange = { _, _ -> }, // Se calcula auto
                    selectedPatient = selectedPatient,
                    // 4. EL BLOQUEO: Si es paciente, pasamos null para que el componente deshabilite la búsqueda
                    onPatientSelected = if (isPatient) null else { it -> selectedPatient = it },
                    selectedTreatment = selectedTreatment,
                    onTreatmentSelected = { selectedTreatment = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    patientViewModel = patientViewModel,
                    treatmentViewModel = treatmentViewModel,
                    appointmentViewModel = appointmentViewModel
                )
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

// ... (No hace falta que borres TimeSlotGrid, EditableChip, SectionLabel y UnavailableChip, déjalos tal y como estaban al final del archivo)