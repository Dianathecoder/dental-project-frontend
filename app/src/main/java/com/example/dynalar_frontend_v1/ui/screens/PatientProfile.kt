package com.example.dynalar_frontend_v1.ui.screens

import PatientHeaderSection
import PatientHeaderSectionApp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfilePage(
    patient: Patient,
    appointmentViewModel: AppointmentViewModel = viewModel(),
    onBackClick: () -> Unit,
    onOdontogramClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onAppointmentClick: (Appointment) -> Unit,
    onCalendarClick: () -> Unit,
    onDateInformationClick: () -> Unit,
    onFilesClick: () -> Unit,
    onDeleteClick: (Long) -> Unit = {},
) {
    LaunchedEffect(patient.id) {
        patient.id?.let { appointmentViewModel.fetchPatientAppointments(it) }
    }

    val uiState = appointmentViewModel.uiStatePatientAppointments
    val patientAppointments = remember(uiState) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data.sortedByDescending { it.startTime }
        } else {
            emptyList()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(27.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    CustomTopBar(title = "Perfil del Pacient", onNavigateBack = onBackClick)
                    IconButton(
                        onClick = { patient.id?.let { onEditClick(it) } },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF0D47A1))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            PatientHeaderSectionApp(
                patient = patient,
                onDelete = { patient.id?.let { onDeleteClick(it) } }
            )


            Spacer(modifier = Modifier.height(24.dp))

            ActionGridSection(
                patient = patient,
                appointmentCount = patientAppointments.size,
                onOdontogramClick = onOdontogramClick,
                onCalendarClick = onCalendarClick,
                onDateInformationClick = onDateInformationClick,
                onFilesClick = onFilesClick,

                )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cites / Tractaments",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )


            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                when (uiState) {
                    is InterfaceGlobal.Loading -> {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF0D47A1))
                            }
                        }
                    }
                    else -> {
                        if (patientAppointments.isEmpty()) {
                            item { EmptyTreatmentsCard() }
                        } else {
                            items(patientAppointments) { appointment ->
                                TreatmentItemRow(
                                    title = appointment.treatment?.name ?: "Tractament",
                                    date = formatDateLabel(appointment.startTime),
                                    onClick = { onAppointmentClick(appointment) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TreatmentItemRow(title: String, date: String, onClick: () -> Unit, ) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, null, tint = Color(0xFF0D47A1))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF334155)
                )


                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = date,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
fun formatDateLabel(dateTime: String?): String {
    if (dateTime == null) return "Sense data"
    return try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val date = LocalDateTime.parse(dateTime, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM yyyy 'a les' HH:mm")
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateTime.take(10)
    }
}

@Composable
fun ActionGridSection(
    patient: Patient,
    appointmentCount: Int,
    onOdontogramClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onFilesClick: () -> Unit, // Asegúrate de que este parámetro esté en la firma
    onDateInformationClick: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard(title = "Odontograma", icon = Icons.Default.Face, modifier = Modifier.weight(1f), onClick = onOdontogramClick)
            ActionCard(title = "Calendari", icon = Icons.Default.DateRange, badgeCount = appointmentCount, modifier = Modifier.weight(1f), onClick = onCalendarClick)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ActionCard(
                title = "Arxius",
                icon = Icons.Default.Folder,
                badgeCount = patient.documents?.size ?: 0,
                modifier = Modifier.weight(1f),
                onClick = onFilesClick
            )
            ActionCard(title = "Historial Clínic", icon = Icons.Default.List, modifier = Modifier.weight(1f), onClick = onDateInformationClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, icon: ImageVector, badgeCount: Int = 0, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.height(100.dp).clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (badgeCount > 0) {
                Badge(modifier = Modifier.align(Alignment.TopEnd), containerColor = Color.Red) {
                    Text(badgeCount.toString(), color = Color.White)
                }
            }
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF0D47A1), modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun EmptyTreatmentsCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, null, tint = Color.LightGray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No hi ha tractaments registrats", color = Color.Gray, fontSize = 14.sp)
        }
    }
}