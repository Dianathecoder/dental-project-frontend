package com.example.dynalar_frontend_v1.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
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
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.viewmodel.AttendanceViewModel

@Composable
fun AuxiliarDashboardPage(
    attendanceViewModel: AttendanceViewModel = viewModel(),
    onNavigatePatients: () -> Unit = {},
    onNavigateAgenda: () -> Unit = {},
    onNavigateMaterials: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var isClockedIn by remember { mutableStateOf(false) } // En la vida real, lo consultaríamos al backend al abrir la app

    val attendanceState by attendanceViewModel.attendanceState.collectAsState()

    LaunchedEffect(attendanceState) {
        if (attendanceState is InterfaceGlobal.Success) {
            val msg = (attendanceState as InterfaceGlobal.Success<String>).data
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            isClockedIn = !isClockedIn // Cambiamos el estado del botón
            attendanceViewModel.resetState()
        } else if (attendanceState is InterfaceGlobal.Error) {
            val error = (attendanceState as InterfaceGlobal.Error).message
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            attendanceViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(title = "Panell Auxiliar", onNavigateBack = onLogout)
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Benvingut/da", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))

            Spacer(modifier = Modifier.height(32.dp))

           //Boton para fichar
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isClockedIn) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                onClick = {
                    if (isClockedIn) attendanceViewModel.clockOut() else attendanceViewModel.clockIn()
                }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (attendanceState is InterfaceGlobal.Loading) {
                        CircularProgressIndicator(color = if (isClockedIn) Color.Red else Color(0xFF4CAF50))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isClockedIn) Color.Red else Color(0xFF4CAF50),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isClockedIn) "Fitxar Sortida" else "Fitxar Entrada",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClockedIn) Color.Red else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))


            Buttons_HomePage(
                onNavigateListPacient = onNavigatePatients,
                onNavigateBoxCalendar = onNavigateAgenda,
                onNavigateBoxMaterials = onNavigateMaterials
            )
        }
    }
}