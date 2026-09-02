package com.example.dynalar_frontend_v1.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.appointment.Appointment
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DentistAgendaPage(
    appointmentViewModel: AppointmentViewModel = viewModel(),
    onNavigateToPatientProfile: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val dentistId = sessionManager.getUserId()

    // Solicitamos las citas de HOY al iniciar la pantalla
    LaunchedEffect(Unit) {
        appointmentViewModel.fetchToday()
    }

    val uiState = appointmentViewModel.uiStateToday

    // Filtramos para asegurarnos de mostrar solo las de este doctor ordenadas por hora
    val myAppointments = remember(uiState) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data
                .filter { it.dentist?.id == dentistId }
                .sortedBy { it.startTime }
        } else {
            emptyList()
        }
    }

    val todayDateStr = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("ca", "ES")))
            .replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = {
            CustomTopBar(title = "La meva agenda", onNavigateBack = onLogout)
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

            Text(
                text = todayDateStr,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Text(
                text = "Aquestes són les teves cites per avui.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState) {
                is InterfaceGlobal.Loading, InterfaceGlobal.Idle -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }
                is InterfaceGlobal.Success -> {
                    if (myAppointments.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No tens cap cita programada per avui. Bon descans!",
                                modifier = Modifier.padding(24.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(myAppointments) { appointment ->
                                DentistAppointmentCard(
                                    appointment = appointment,
                                    onClick = {
                                        // Navegamos directo a la ficha del paciente
                                        appointment.patient?.id?.let { onNavigateToPatientProfile(it) }
                                    }
                                )
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Text("Error al carregar l'agenda.", color = Color.Red)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DentistAppointmentCard(appointment: Appointment, onClick: () -> Unit) {
    val timeStr = appointment.startTime?.split("T", " ")?.lastOrNull()?.take(5) ?: "--:--"
    val patientName = "${appointment.patient?.name ?: "Pacient"} ${appointment.patient?.lastName ?: ""}".trim()
    val treatmentName = appointment.treatment?.name ?: "Revisió general"
    val boxInfo = appointment.box?.number?.let { "Box $it" } ?: "Sense Box"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora destacada
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeStr,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5BB2),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Paciente y Box
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = boxInfo,
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = treatmentName,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}