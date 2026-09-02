package com.example.dynalar_frontend_v1.ui.screens.appointment

import PatientHeaderSectionApp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.AppointmentFormContent
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ResumeDateScreen(
    appointment: Appointment,
    onBackClick: () -> Unit,
    onPatientClick: (Long) -> Unit,
    appointmentViewModel: AppointmentViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel()
) {
    // 1. MIRAMOS QUIÉN ESTÁ USANDO EL TELÉFONO
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isPatient = sessionManager.hasRole("PATIENT") || sessionManager.hasRole("ROLE_PATIENT")

    LaunchedEffect(Unit) {
        appointmentViewModel.selectedAppointment = appointment
        if (treatmentViewModel.uiStateTreatment is InterfaceGlobal.Idle) {
            treatmentViewModel.getTreatments()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.statusBarsPadding())
                CustomTopBar(title = stringResource(R.string.appointment_summary_title), onNavigateBack = onBackClick)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                appointment.patient?.let { patient ->
                    // 2. SI ES PACIENTE, LA CABECERA NO SE PUEDE CLICAR NI BORRAR
                    PatientHeaderSectionApp(
                        patient = patient,
                        onClick = { if (!isPatient) patient.id?.let { id -> onPatientClick(id) } },
                        onDelete = {
                            if (!isPatient) {
                                patient.id?.let { id ->
                                    patientViewModel.deletePatient(id)
                                    onBackClick()
                                }
                            }
                        },
                        onGoToProfile = { if (!isPatient) patient.id?.let { id -> onPatientClick(id) } }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppointmentDetailsCard(
                    appointment = appointment,
                    isPatient = isPatient, // 3. LE PASAMOS EL ROL A LA TARJETA
                    appointmentViewModel = appointmentViewModel,
                    treatmentViewModel = treatmentViewModel,
                    patientViewModel = patientViewModel,
                    onSave = { updated ->
                        appointmentViewModel.updateAppointment(updated)
                    },
                    onDelete = {
                        appointment.id?.let { id ->
                            appointmentViewModel.deleteAppointment(id)
                            onBackClick()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun AppointmentDetailsCard(
    appointment: Appointment,
    isPatient: Boolean, // <--- Recibimos el rol
    appointmentViewModel: AppointmentViewModel,
    treatmentViewModel: TreatmentViewModel,
    patientViewModel: PatientViewModel,
    onSave: (Appointment) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 4. CÁLCULO DE LAS 24 HORAS (Solo sirve si es paciente)
    val canCancel = remember(appointment.startTime) {
        try {
            val appointmentTime = LocalDateTime.parse(appointment.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val now = LocalDateTime.now()
            val hoursUntil = Duration.between(now, appointmentTime).toHours()
            hoursUntil >= 24
        } catch (e: Exception) {
            false
        }
    }

    // Estados locales para la edición
    var editedDate by remember(appointment) {
        mutableStateOf(LocalDate.parse(appointment.startTime?.split("T")?.firstOrNull() ?: LocalDate.now().toString()))
    }

    val timeParts = appointment.startTime?.split("T")?.lastOrNull()?.split(":")
    var hour by remember(appointment) { mutableIntStateOf(timeParts?.getOrNull(0)?.toInt() ?: 9) }
    var minute by remember(appointment) { mutableIntStateOf(timeParts?.getOrNull(1)?.toInt() ?: 0) }
    var editedTreatment by remember(appointment) { mutableStateOf(appointment.treatment) }
    var editedNotes by remember(appointment) { mutableStateOf(appointment.reason ?: "") }

    val totalMin = hour * 60 + minute + (editedTreatment?.durationMinutes ?: 30) + 5
    val endHour = (totalMin / 60) % 24
    val endMinute = totalMin % 60

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isPatient) "Cancel·lar Cita" else stringResource(R.string.appointment_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(if (isPatient) "Estàs segur que vols cancel·lar aquesta cita?" else stringResource(R.string.appointment_delete_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.btn_delete), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.appointment_details_title),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // 5. SI ES PACIENTE, OCULTAMOS EL LÁPIZ DE EDITAR
                if (!isEditing && !isPatient) {
                    Row {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar", tint = ButtonPrimary)
                        }
                    }
                }
            }

            // 6. MODO EDICIÓN (SOLO TRABAJADORES)
            if (isEditing && !isPatient) {
                AppointmentFormContent(
                    selectedDate = editedDate,
                    onDateChange = { editedDate = it },
                    hour = hour,
                    minute = minute,
                    onStartTimeChange = { h, m -> hour = h; minute = m },
                    endHour = endHour,
                    endMinute = endMinute,
                    onEndTimeChange = { _, _ -> },
                    selectedPatient = appointment.patient,
                    onPatientSelected = null,
                    selectedTreatment = editedTreatment,
                    onTreatmentSelected = { editedTreatment = it },
                    description = editedNotes,
                    onDescriptionChange = { editedNotes = it },
                    patientViewModel = patientViewModel,
                    treatmentViewModel = treatmentViewModel,
                    appointmentViewModel = appointmentViewModel
                )

                Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Navegate_Button(
                        text = "Guardar",
                        onClick = {
                            val newTime = "${editedDate}T${"%02d:%02d".format(hour, minute)}:00"
                            onSave(appointment.copy(startTime = newTime, treatment = editedTreatment, reason = editedNotes))
                            isEditing = false
                        },
                        backgroundColor = ButtonPrimary,
                        modifier = Modifier.weight(1f).height(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }

            } else {
                // 7. MODO LECTURA (Lo ven tanto trabajadores como pacientes)
                DetailRow(label = stringResource(R.string.appointment_date_label), value = editedDate.toString())

                val toWord = stringResource(R.string.appointment_time_to)
                DetailRow(label = stringResource(R.string.appointment_time_label), value = "%02d:%02d $toWord %02d:%02d".format(hour, minute, endHour, endMinute))

                DetailRow(label = stringResource(R.string.appointment_treatment_label), value = editedTreatment?.name ?: stringResource(R.string.appointment_none))

                DetailRow(
                    label = stringResource(R.string.appointment_notes_label),
                    value = if (editedNotes.isNullOrBlank()) stringResource(R.string.appointment_no_notes) else editedNotes
                )

                // 8. BOTÓN CANCELAR (SOLO PARA PACIENTES) CON REGLA 24H
                if (isPatient) {
                    Spacer(modifier = Modifier.height(24.dp))
                    if (canCancel) {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel·lar cita", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Per cancel·lar amb menys de 24 hores d'antelació, truca a la clínica.",
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 16.sp, color = Color.DarkGray)
    }
}