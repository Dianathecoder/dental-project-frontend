package com.example.dynalar_frontend_v1.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.screens.EditableChip
import com.example.dynalar_frontend_v1.ui.screens.SectionLabel
import com.example.dynalar_frontend_v1.ui.screens.TimeSlotGrid
import com.example.dynalar_frontend_v1.ui.screens.UnavailableChip
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.TextoPrincipal
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList
import com.example.dynalar_frontend_v1.R
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayAppointmentsDialog(
    date: LocalDate,
    appointments: List<Appointment>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Citas del $dateStr",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextoPrincipal
            )
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ButtonPrimary)
                }
            } else if (appointments.isEmpty()) {
                Text(
                    text = "No hay citas programadas para este día.",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(appointments) { appointment ->
                        val time = appointment.startTime?.split("T", " ")?.lastOrNull()?.take(5) ?: "--:--"
                        val patientName = "${appointment.patient?.name ?: ""} ${appointment.patient?.lastName ?: ""}".trim()
                        val allergies = appointment.patient?.medicalRecord?.allergies
                        val treatmentName = appointment.treatment?.name
                        val infectiousDeceases = appointment.patient?.medicalRecord?.infectiousDeceases

                        val hasInfections = !infectiousDeceases.isNullOrBlank()
                        val hasAllergies = !allergies.isNullOrBlank()

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onAppointmentClick(appointment)
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = ButtonPrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = time,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = ButtonPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = patientName.ifEmpty { "Paciente Desconocido" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextoPrincipal
                                        )
                                    }

                                    // --- LA FLECHA MEJORADA ---
                                    Surface(
                                        shape = CircleShape,
                                        color = ButtonPrimary.copy(alpha = 0.1f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForwardIos,
                                                contentDescription = "Ver detalles",
                                                tint = ButtonPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (!treatmentName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tractament: $treatmentName",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }


                                if (hasInfections || hasAllergies) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {


                                        if (hasInfections) {
                                            Surface(
                                                color = Color.Red.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Coronavirus,
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = infectiousDeceases!!,
                                                        color = Color.Red,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }


                                        if (hasAllergies) {
                                            Surface(
                                                color = Color(0xFFE65100).copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFFE65100).copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color(0xFFE65100),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Al·lèrgies: $allergies",
                                                        color = Color(0xFFE65100),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tancar", color = ButtonPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
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
    description: String,
    onDescriptionChange: (String) -> Unit,
    patientViewModel: PatientViewModel,
    treatmentViewModel: TreatmentViewModel,
    appointmentViewModel: AppointmentViewModel
) {
    var showCalendar by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        treatmentViewModel.getTreatments()
        if (onPatientSelected != null) {
            patientViewModel.getPatients()
        }
    }


    LaunchedEffect(selectedTreatment, selectedDate, selectedPatient) {
        val pId = selectedPatient?.id
        val tId = selectedTreatment?.id

        if (pId != null && tId != null) {
            val start = selectedDate.with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            appointmentViewModel.fetchSlots(pId, tId, start, end)
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
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, d 'de' MMM", Locale("es"))),
                onClick = { showCalendar = true }
            )
            Spacer(Modifier.width(10.dp))
            EditableChip(text = "%02d:%02d".format(hour, minute), onClick = {  })
            Spacer(Modifier.width(8.dp))
            Text("→", color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            EditableChip(
                text = if (selectedTreatment != null) "%02d:%02d".format(endHour, endMinute) else "--:--",
                onClick = { }
            )
        }


        if (onPatientSelected != null) {
            Spacer(Modifier.height(28.dp))
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

        // 3. TRATAMIENTO
        Spacer(Modifier.height(24.dp))
        SectionLabel(icon = R.drawable.visita_tratamientos, text = "Tratamiento")
        when (val tState = treatmentViewModel.uiStateTreatment) {
            is InterfaceGlobal.Success -> {
                // Filtramos los tratamientos si el paciente no ha firmado la anestesia
                // Verificamos tanto el campo booleano como la existencia de la firma en el registro médico
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


        if (selectedTreatment != null) {
            Spacer(Modifier.height(28.dp))
            SectionLabel(icon = R.drawable.visita_tiempo, text = "Horaris Disponibles")
            when (val slotsState = appointmentViewModel.uiStateSlots) {
                is InterfaceGlobal.Success -> {
                    val todaySlots = slotsState.data[selectedDate.toString()] ?: emptyList()
                    if (todaySlots.isEmpty()) {
                        UnavailableChip("Sense forats lliures")
                    } else {
                        TimeSlotGrid(todaySlots, hour, minute) { h, m -> onStartTimeChange(h, m) }
                    }
                }
                is InterfaceGlobal.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
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