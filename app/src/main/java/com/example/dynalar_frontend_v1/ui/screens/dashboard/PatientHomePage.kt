package com.example.dynalar_frontend_v1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
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
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.CardMenuButton
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PatientHomePage(
    appointmentViewModel: AppointmentViewModel = viewModel(),
    onNavigateRequestAppointment: () -> Unit,
    onNavigateMyDocuments: (Long) -> Unit, // Le pasamos el ID para cargar sus archivos
    onNavigateAppointmentDetail: (Appointment) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val patientId = sessionManager.getUserId()

    // Pedimos las citas del paciente al cargar la pantalla
    LaunchedEffect(patientId) {
        if (patientId != -1L) {
            appointmentViewModel.fetchPatientAppointments(patientId)
        }
    }

    val uiState = appointmentViewModel.uiStatePatientAppointments

    // Filtramos para mostrar solo las futuras/pendientes
    val upcomingAppointments = remember(uiState) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data
                .filter { it.startTime != null && LocalDateTime.parse(it.startTime).isAfter(LocalDateTime.now()) }
                .sortedBy { it.startTime }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(title = "El Meu Espai", onNavigateBack = onLogout)
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. ZONA DE ACCIONES RÁPIDAS
            Text(
                text = "Què necessites fer?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardMenuButton(
                    icon = Icons.Default.CalendarMonth,
                    title = "Demanar Cita",
                    onClick = onNavigateRequestAppointment,
                    modifier = Modifier.weight(1f)
                )
                CardMenuButton(
                    icon = Icons.Default.FolderShared,
                    title = "Els meus arxius",
                    onClick = { onNavigateMyDocuments(patientId) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 2. ZONA DE PRÓXIMAS CITAS
            Text(
                text = "Les meves properes cites",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is InterfaceGlobal.Loading, InterfaceGlobal.Idle -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }
                is InterfaceGlobal.Success -> {
                    if (upcomingAppointments.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No tens cap cita programada properament.",
                                modifier = Modifier.padding(24.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(upcomingAppointments) { appointment ->
                                PatientAppointmentCard(
                                    appointment = appointment,
                                    onClick = { onNavigateAppointmentDetail(appointment) }
                                )
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Text("Error al carregar les cites", color = Color.Red)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PatientAppointmentCard(appointment: Appointment, onClick: () -> Unit) {
    val dateTime = appointment.startTime?.let {
        LocalDateTime.parse(it).format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))
    } ?: "Data pendent"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = ButtonPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = appointment.treatment?.name ?: "Revisió general",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateTime,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                if (appointment.dentist != null) {
                    Text(
                        text = "Dr/a. ${appointment.dentist.surname}",
                        color = ButtonPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}