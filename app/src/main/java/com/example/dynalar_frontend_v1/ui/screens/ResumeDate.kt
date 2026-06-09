package com.example.dynalar_frontend_v1.ui.screens

import PatientHeaderSectionApp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.ui.components.AppointmentFormContent
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import java.time.LocalDate

@Composable
fun ResumeDateScreen(
    appointment: Appointment,
    onBackClick: () -> Unit,
    onPatientClick: (Long) -> Unit,
    appointmentViewModel: AppointmentViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel()
) {
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
                CustomTopBar(title = "Resum de la visita", onNavigateBack = onBackClick)
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
                    PatientHeaderSectionApp(
                        patient = patient,
                        onClick = { patient.id?.let { id -> onPatientClick(id) } },
                        onDelete = {
                            patient.id?.let { id ->
                                patientViewModel.deletePatient(id)
                                onBackClick()
                            }
                        },
                        onGoToProfile = { patient.id?.let { id -> onPatientClick(id) } }

                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                AppointmentDetailsCard(
                    appointment = appointment,
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
    appointmentViewModel: AppointmentViewModel,
    treatmentViewModel: TreatmentViewModel,
    patientViewModel: PatientViewModel,
    onSave: (Appointment) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) } // Estado para el diálogo de seguridad

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

    // --- DIÁLOGO DE SEGURIDAD ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar cita", fontWeight = FontWeight.Bold) },
            text = { Text("Estàs segur que vols eliminar aquesta cita? Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel·lar")
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
                Text("Detalls de la Cita", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (!isEditing) {
                    Row {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar", tint = ButtonPrimary)
                        }
                    }
                }
            }

            if (isEditing) {
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
                    TextButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Cancel·lar") }
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
            } else {
                // VISTA DE LECTURA
                DetailRow(label = "Data:", value = editedDate.toString())
                DetailRow(label = "Horari:", value = "%02d:%02d a %02d:%02d".format(hour, minute, endHour, endMinute))
                DetailRow(label = "Tractament:", value = editedTreatment?.name ?: "Cap")

                // Mostramos la nota guardada o el texto por defecto
                DetailRow(
                    label = "Observacions:",
                    value = if (editedNotes.isNullOrBlank()) "Cap observació" else editedNotes
                )



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