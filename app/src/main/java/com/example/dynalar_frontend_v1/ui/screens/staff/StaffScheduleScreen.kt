package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.dynalar_frontend_v1.model.staff.AbsenceEvent
import com.example.dynalar_frontend_v1.model.staff.AbsenceType
import com.example.dynalar_frontend_v1.ui.components.AbsenceEventCard
import com.example.dynalar_frontend_v1.ui.components.AbsencesCalendarView
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.StaffControlViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StaffScheduleScreen(
    staffId: Long,
    staffControlViewModel: StaffControlViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(staffId, currentMonth) {
        userViewModel.getUserById(staffId)
        staffControlViewModel.fetchMonthlyAbsences(currentMonth)
    }

    val selectedStaff = userViewModel.selectedUser
    val staffFullName = "${selectedStaff?.name ?: ""} ${selectedStaff?.surname ?: ""}".trim()

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(27.dp))
                CustomTopBar(
                    title = if (staffFullName.isNotBlank()) "Calendari de $staffFullName" else "Calendari Laboral",
                    onNavigateBack = onNavigateBack
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = staffControlViewModel.uiStateAbsences) {
                is InterfaceGlobal.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }
                is InterfaceGlobal.Success -> {
                    val employeeEvents = state.data.map { dto ->
                        AbsenceEvent(
                            id = dto.id,
                            title = dto.title,
                            staffName = dto.staffName,
                            type = try {
                                AbsenceType.valueOf(dto.type.uppercase())
                            } catch (e: Exception) {
                                AbsenceType.HOLIDAY
                            },
                            startDate = LocalDate.parse(dto.startDate),
                            endDate = LocalDate.parse(dto.endDate)
                        )
                    }.filter { event ->
                        val eventName = event.staffName?.trim() ?: ""
                        eventName.contains(selectedStaff?.name ?: "", ignoreCase = true) ||
                                (selectedStaff?.surname != null && eventName.contains(selectedStaff.surname!!, ignoreCase = true))
                    }

                    AbsencesCalendarView(
                        currentMonth = currentMonth,
                        events = employeeEvents,
                        onMonthChange = { currentMonth = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Vacances i Ausències Registrades",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (employeeEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sense vacances o ausències registrades per a aquest mes.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(employeeEvents) { event ->
                                AbsenceEventCard(event = event)
                            }
                        }
                    }
                }
                is InterfaceGlobal.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Error al carregar el calendari", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}
