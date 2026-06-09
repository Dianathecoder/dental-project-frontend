package com.example.dynalar_frontend_v1.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.AppointmentFormContent
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
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
    var selectedDate by remember { mutableStateOf(initialDate) }
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    // Calcular fin automáticamente
    var selectedTreatment by remember { mutableStateOf<Treatment?>(null) }
    val margen = 5
    val totalMinutes = hour * 60 + minute + (selectedTreatment?.durationMinutes ?: 30) + margen
    val endHour = (totalMinutes / 60) % 24
    val endMinute = totalMinutes % 60

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var description by remember { mutableStateOf("") }

    // --- ESTADO PARA ADVERTENCIA DE ENFERMEDADES ---
    var showInfectionWarning by remember { mutableStateOf(false) }
    var hasAcceptedInfectionWarning by remember { mutableStateOf(false) }

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
            title = { Text("Avís Mèdic", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text("Aquest pacient té registrades malalties infeccioses: ${selectedPatient?.medicalRecord?.infectiousDeceases}. \n\nSi us plau, prengui les mesures de seguretat necessàries.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInfectionWarning = false
                        hasAcceptedInfectionWarning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                ) {
                    Text("Entès")
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
                    text = if (isLoading) "Assignant..." else "Confirmar Cita",
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
            CustomTopBar(title = "Nova Cita", onNavigateBack = onBackClick)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppointmentFormContent(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    hour = hour, minute = minute,
                    onStartTimeChange = { h, m -> hour = h; minute = m },
                    endHour = endHour, endMinute = endMinute,
                    onEndTimeChange = { _, _ -> }, // Se calcula auto
                    selectedPatient = selectedPatient,
                    onPatientSelected = { selectedPatient = it },
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
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF455A64)
                )
            }
        }
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
fun SectionLabel(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
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