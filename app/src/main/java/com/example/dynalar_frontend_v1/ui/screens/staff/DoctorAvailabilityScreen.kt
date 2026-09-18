package com.example.dynalar_frontend_v1.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.model.management.Treatment
import com.example.dynalar_frontend_v1.model.staff.dentist.DentistAvailabilityDTO
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.UserAvatar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

data class TimeSlot(var start: String, var end: String)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DoctorAvailabilityScreen(
    doctorId: Long = -1L,
    userViewModel: UserViewModel = viewModel(),
    treatmentViewModel: TreatmentViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToStaffProfile: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.getAllStaff()
        treatmentViewModel.getTreatments()
    }

    val doctorList = userViewModel.staffList
    var selectedDoctor by remember(doctorList, doctorId) {
        mutableStateOf<User?>(doctorList.find { it.id == doctorId } ?: doctorList.firstOrNull())
    }

    val availabilityState = userViewModel.dentistAvailability
    var dto by remember { mutableStateOf(createEmptyDto()) }
    var scheduleType by remember { mutableStateOf(2) } // Por defecto Parcial
    var selectedTreatmentIds by remember { mutableStateOf(emptySet<Long>()) }

    var pendingScheduleType by remember { mutableStateOf<Int?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val treatments: List<Treatment> = treatmentViewModel.treatmentList

    var showTimePicker by remember { mutableStateOf(false) }
    var timePickerState by remember { mutableStateOf(TimePickerState(0, 0, true)) }
    var onTimeSelectedCallback by remember { mutableStateOf<(String) -> Unit>({}) }

    // RECARGAR DATOS AL CAMBIAR DE DOCTOR
    LaunchedEffect(selectedDoctor) {
        selectedDoctor?.id?.let { id ->
            dto = createEmptyDto()
            selectedTreatmentIds = emptySet()
            scheduleType = 2
            userViewModel.fetchDentistAvailability(id)
        } ?: run {
            dto = createEmptyDto()
            selectedTreatmentIds = emptySet()
            scheduleType = 2
        }
    }

    // SINCRONIZAR CUANDO LLEGAN LOS DATOS DEL SERVIDOR Y DETECTAR TIPO DE JORNADA CORRECTAMENTE
    LaunchedEffect(availabilityState) {
        if (availabilityState != null) {
            dto = availabilityState
            selectedTreatmentIds = availabilityState.treatmentIds?.toSet() ?: emptySet()
            scheduleType = detectScheduleType(availabilityState)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF8FAFC))) {
                Spacer(modifier = Modifier.height(27.dp))
                CustomTopBar(title = "Configuració Clínica", onNavigateBack = onNavigateBack)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ZONA DOCTORES / EMPLEADOS
            if (doctorList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(doctorList, key = { it.id ?: 0L }) { doc ->
                        val isSelected = selectedDoctor?.id == doc.id
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ButtonPrimary else Color(0xFFE2E8F0)),
                            modifier = Modifier.width(90.dp).clickable { selectedDoctor = doc }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UserAvatar(
                                    avatarUrl = doc.avatarUrl, userId = doc.id, sexRaw = doc.sex, rolesRaw = doc.roles,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = doc.name ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) ButtonPrimary else Color(0xFF1E293B), maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedDoctor?.let { doc ->
                val displayRole = doc.roles.firstOrNull()?.replace("ROLE_", "")?.uppercase() ?: "PERSONAL"

                // Comprobación de rol para mostrar tratamientos (SuperAdmin, Owner, Admin, Doctor/Dentist)
                val docRoles = doc.roles.map { it.uppercase().replace("ROLE_", "") }
                val showTreatments = docRoles.any { it in listOf("SUPERADMIN", "OWNER", "ADMIN", "DOCTOR", "DENTIST") }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().clickable { doc.id?.let { onNavigateToStaffProfile(it) } }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(avatarUrl = doc.avatarUrl, userId = doc.id, sexRaw = doc.sex, rolesRaw = doc.roles, modifier = Modifier.size(50.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("${doc.name} ${doc.surname}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(displayRole, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ButtonPrimary)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Veure Perfil", fontSize = 11.sp, color = ButtonPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN DE TRATAMIENTOS ASIGNADOS (SÓLO LECTURA)
                if (showTreatments) {
                    Text("Tractaments Assignats", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))

                    val assignedTreatments = treatments.filter { selectedTreatmentIds.contains(it.id ?: 0L) }

                    if (assignedTreatments.isEmpty()) {
                        Text("Cap tractament assignat actualment.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(assignedTreatments, key = { it.id ?: 0L }) { treatment ->
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, ButtonPrimary)
                                ) {
                                    Text(
                                        text = treatment.name ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ButtonPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Tipus de Jornada", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleTypeButton("Completa", scheduleType == 1, { if (scheduleType != 1) pendingScheduleType = 1 }, Modifier.weight(1f))
                    ScheduleTypeButton("Parcial", scheduleType == 2, { if (scheduleType == 1) pendingScheduleType = 2 else scheduleType = 2 }, Modifier.weight(1f))
                    ScheduleTypeButton("Específic", scheduleType == 3, { if (scheduleType == 1) pendingScheduleType = 3 else scheduleType = 3 }, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ZONA DE HORARIOS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (scheduleType == 1) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Jornada Completa Activada", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Dilluns a Divendres\n09:00 - 14:00  |  16:00 - 20:00", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
                            }
                        } else if (scheduleType == 2) {
                            DayShiftRow("Dilluns", dto.mondayMorningActive, dto.mondayAfternoonActive) { m, a -> dto = dto.copy(mondayMorningActive = m, mondayAfternoonActive = a) }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            DayShiftRow("Dimarts", dto.tuesdayMorningActive, dto.tuesdayAfternoonActive) { m, a -> dto = dto.copy(tuesdayMorningActive = m, tuesdayAfternoonActive = a) }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            DayShiftRow("Dimecres", dto.wednesdayMorningActive, dto.wednesdayAfternoonActive) { m, a -> dto = dto.copy(wednesdayMorningActive = m, wednesdayAfternoonActive = a) }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            DayShiftRow("Dijous", dto.thursdayMorningActive, dto.thursdayAfternoonActive) { m, a -> dto = dto.copy(thursdayMorningActive = m, thursdayAfternoonActive = a) }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            DayShiftRow("Divendres", dto.fridayMorningActive, dto.fridayAfternoonActive) { m, a -> dto = dto.copy(fridayMorningActive = m, fridayAfternoonActive = a) }
                        } else {
                            val openTimePicker: (String, (String) -> Unit) -> Unit = { currentTime, onSelected ->
                                val parts = currentTime.split(":")
                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                timePickerState = TimePickerState(h, m, true)
                                onTimeSelectedCallback = onSelected
                                showTimePicker = true
                            }

                            var mondayShifts by remember(dto) { mutableStateOf(getShiftsFromDto(dto, "Monday")) }
                            DynamicDayRow("Dilluns", mondayShifts, { mondayShifts = it; dto = mapShiftsToDto(dto, "Monday", it) }, openTimePicker)
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            var tuesdayShifts by remember(dto) { mutableStateOf(getShiftsFromDto(dto, "Tuesday")) }
                            DynamicDayRow("Dimarts", tuesdayShifts, { tuesdayShifts = it; dto = mapShiftsToDto(dto, "Tuesday", it) }, openTimePicker)
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            var wednesdayShifts by remember(dto) { mutableStateOf(getShiftsFromDto(dto, "Wednesday")) }
                            DynamicDayRow("Dimecres", wednesdayShifts, { wednesdayShifts = it; dto = mapShiftsToDto(dto, "Wednesday", it) }, openTimePicker)
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            var thursdayShifts by remember(dto) { mutableStateOf(getShiftsFromDto(dto, "Thursday")) }
                            DynamicDayRow("Dijous", thursdayShifts, { thursdayShifts = it; dto = mapShiftsToDto(dto, "Thursday", it) }, openTimePicker)
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            var fridayShifts by remember(dto) { mutableStateOf(getShiftsFromDto(dto, "Friday")) }
                            DynamicDayRow("Divendres", fridayShifts, { fridayShifts = it; dto = mapShiftsToDto(dto, "Friday", it) }, openTimePicker)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // BOTONES DE ACCIÓN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F))
                            ) {
                                Text("Esborrar Tot", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val docId = doc.id ?: return@Button

                                    val finalDto = when (scheduleType) {
                                        1 -> dto.copy(
                                            mondayMorningActive = true, mondayMorningStart = "09:00", mondayMorningEnd = "14:00",
                                            mondayAfternoonActive = true, mondayAfternoonStart = "16:00", mondayAfternoonEnd = "20:00",
                                            mondayEveningActive = false,
                                            tuesdayMorningActive = true, tuesdayMorningStart = "09:00", tuesdayMorningEnd = "14:00",
                                            tuesdayAfternoonActive = true, tuesdayAfternoonStart = "16:00", tuesdayAfternoonEnd = "20:00",
                                            tuesdayEveningActive = false,
                                            wednesdayMorningActive = true, wednesdayMorningStart = "09:00", wednesdayMorningEnd = "14:00",
                                            wednesdayAfternoonActive = true, wednesdayAfternoonStart = "16:00", wednesdayAfternoonEnd = "20:00",
                                            wednesdayEveningActive = false,
                                            thursdayMorningActive = true, thursdayMorningStart = "09:00", thursdayMorningEnd = "14:00",
                                            thursdayAfternoonActive = true, thursdayAfternoonStart = "16:00", thursdayAfternoonEnd = "20:00",
                                            thursdayEveningActive = false,
                                            fridayMorningActive = true, fridayMorningStart = "09:00", fridayMorningEnd = "14:00",
                                            fridayAfternoonActive = true, fridayAfternoonStart = "16:00", fridayAfternoonEnd = "20:00",
                                            fridayEveningActive = false
                                        )
                                        2 -> dto.copy(
                                            mondayMorningStart = "09:00", mondayMorningEnd = "14:00", mondayAfternoonStart = "15:00", mondayAfternoonEnd = "20:00", mondayEveningActive = false,
                                            tuesdayMorningStart = "09:00", tuesdayMorningEnd = "14:00", tuesdayAfternoonStart = "15:00", tuesdayAfternoonEnd = "20:00", tuesdayEveningActive = false,
                                            wednesdayMorningStart = "09:00", wednesdayMorningEnd = "14:00", wednesdayAfternoonStart = "15:00", wednesdayAfternoonEnd = "20:00", wednesdayEveningActive = false,
                                            thursdayMorningStart = "09:00", thursdayMorningEnd = "14:00", thursdayAfternoonStart = "15:00", thursdayAfternoonEnd = "20:00", thursdayEveningActive = false,
                                            fridayMorningStart = "09:00", fridayMorningEnd = "14:00", fridayAfternoonStart = "15:00", fridayAfternoonEnd = "20:00", fridayEveningActive = false
                                        )
                                        else -> dto
                                    }.copy(treatmentIds = selectedTreatmentIds.toList())

                                    userViewModel.updateDentistAvailability(docId, finalDto) {
                                        Toast.makeText(context, "Configuració desada correctament", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                            ) {
                                Text("Desar Canvis", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingScheduleType != null) {
        AlertDialog(
            onDismissRequest = { pendingScheduleType = null },
            title = { Text("Canvi de Jornada", fontWeight = FontWeight.Bold) },
            text = { Text("Si canvies a o des de Jornada Completa s'esborraran els horaris configurats actualment per evitar conflictes. Vols continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        scheduleType = pendingScheduleType!!
                        dto = createEmptyDto()
                        pendingScheduleType = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                ) { Text("Sí, canviar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingScheduleType = null }) { Text("Cancel·lar", color = Color.Gray) }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Esborrar Configuració", fontWeight = FontWeight.Bold) },
            text = { Text("Estàs segur que vols esborrar tots els horaris d'aquest treballador? Aquesta acció no es pot desfer.") },
            confirmButton = {
                Button(
                    onClick = {
                        dto = createEmptyDto()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Esborrar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel·lar", color = Color.Gray) }
            }
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelectedCallback(formattedTime)
                    showTimePicker = false
                }) { Text("Acceptar", color = ButtonPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel·lar", color = Color.Gray) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}


fun createEmptyDto() = DentistAvailabilityDTO(
    mondayMorningActive = false, mondayAfternoonActive = false, mondayEveningActive = false,
    tuesdayMorningActive = false, tuesdayAfternoonActive = false, tuesdayEveningActive = false,
    wednesdayMorningActive = false, wednesdayAfternoonActive = false, wednesdayEveningActive = false,
    thursdayMorningActive = false, thursdayAfternoonActive = false, thursdayEveningActive = false,
    fridayMorningActive = false, fridayAfternoonActive = false, fridayEveningActive = false,
    treatmentIds = emptyList()
)

fun detectScheduleType(dto: DentistAvailabilityDTO): Int {
    val isFull = dto.mondayMorningActive && dto.mondayAfternoonActive &&
            dto.tuesdayMorningActive && dto.tuesdayAfternoonActive &&
            dto.wednesdayMorningActive && dto.wednesdayAfternoonActive &&
            dto.thursdayMorningActive && dto.thursdayAfternoonActive &&
            dto.fridayMorningActive && dto.fridayAfternoonActive &&
            dto.mondayMorningStart == "09:00" && dto.mondayMorningEnd == "14:00" &&
            dto.mondayAfternoonStart == "16:00" && dto.mondayAfternoonEnd == "20:00" &&
            dto.tuesdayMorningStart == "09:00" && dto.tuesdayMorningEnd == "14:00" &&
            dto.tuesdayAfternoonStart == "16:00" && dto.tuesdayAfternoonEnd == "20:00" &&
            dto.wednesdayMorningStart == "09:00" && dto.wednesdayMorningEnd == "14:00" &&
            dto.wednesdayAfternoonStart == "16:00" && dto.wednesdayAfternoonEnd == "20:00" &&
            dto.thursdayMorningStart == "09:00" && dto.thursdayMorningEnd == "14:00" &&
            dto.thursdayAfternoonStart == "16:00" && dto.thursdayAfternoonEnd == "20:00" &&
            dto.fridayMorningStart == "09:00" && dto.fridayMorningEnd == "14:00" &&
            dto.fridayAfternoonStart == "16:00" && dto.fridayAfternoonEnd == "20:00"
    if (isFull) return 1

    if (dto.mondayEveningActive || dto.tuesdayEveningActive || dto.wednesdayEveningActive || dto.thursdayEveningActive || dto.fridayEveningActive) return 3

    val checkCustom = { active: Boolean, start: String?, end: String?, stdStart: String, stdEnd: String ->
        active && (start != stdStart || end != stdEnd)
    }

    val isCustom = checkCustom(dto.mondayMorningActive, dto.mondayMorningStart, dto.mondayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.mondayAfternoonActive, dto.mondayAfternoonStart, dto.mondayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.tuesdayMorningActive, dto.tuesdayMorningStart, dto.tuesdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.tuesdayAfternoonActive, dto.tuesdayAfternoonStart, dto.tuesdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.wednesdayMorningActive, dto.wednesdayMorningStart, dto.wednesdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.wednesdayAfternoonActive, dto.wednesdayAfternoonStart, dto.wednesdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.thursdayMorningActive, dto.thursdayMorningStart, dto.thursdayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.thursdayAfternoonActive, dto.thursdayAfternoonStart, dto.thursdayAfternoonEnd, "15:00", "20:00") ||
            checkCustom(dto.fridayMorningActive, dto.fridayMorningStart, dto.fridayMorningEnd, "09:00", "14:00") ||
            checkCustom(dto.fridayAfternoonActive, dto.fridayAfternoonStart, dto.fridayAfternoonEnd, "15:00", "20:00")

    if (isCustom) return 3

    return 2
}

@Composable
fun DynamicDayRow(
    dayName: String,
    shifts: List<TimeSlot>,
    onShiftsChange: (List<TimeSlot>) -> Unit,
    onOpenTimePicker: (String, (String) -> Unit) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = dayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0D47A1))
        Spacer(modifier = Modifier.height(12.dp))

        if (shifts.isEmpty()) {
            Text("No treballa", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        } else {
            shifts.forEachIndexed { index, slot ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                ) {
                    Switch(
                        checked = true,
                        onCheckedChange = {
                            val newShifts = shifts.toMutableList()
                            newShifts.removeAt(index)
                            onShiftsChange(newShifts)
                        },
                        modifier = Modifier.scale(0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Torn ${index + 1}", fontSize = 14.sp, modifier = Modifier.width(46.dp), fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.width(4.dp))
                    TimeSelector(slot.start) { onOpenTimePicker(slot.start) { newTime -> val s = shifts.toMutableList(); s[index] = slot.copy(start = newTime); onShiftsChange(s) } }
                    Text(" a ", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 13.sp, color = Color.Gray)
                    TimeSelector(slot.end) { onOpenTimePicker(slot.end) { newTime -> val s = shifts.toMutableList(); s[index] = slot.copy(end = newTime); onShiftsChange(s) } }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { val newShifts = shifts.toMutableList(); newShifts.removeAt(index); onShiftsChange(newShifts) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.RemoveCircle, contentDescription = "Eliminar Torn", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }

        if (shifts.size < 3) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                    val newShifts = shifts.toMutableList()
                    val defStart = if (shifts.isEmpty()) "09:00" else if (shifts.size == 1) "15:00" else "20:00"
                    val defEnd = if (shifts.isEmpty()) "14:00" else if (shifts.size == 1) "20:00" else "22:00"
                    newShifts.add(TimeSlot(defStart, defEnd))
                    onShiftsChange(newShifts)
                }.padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ButtonPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Afegir Torn", color = ButtonPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScheduleTypeButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ButtonPrimary else Color.White,
        border = BorderStroke(1.dp, if (isSelected) ButtonPrimary else Color(0xFFE2E8F0)),
        modifier = modifier.height(44.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = if (isSelected) Color.White else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DayShiftRow(dayName: String, morning: Boolean, afternoon: Boolean, onChanged: (Boolean, Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = dayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B), modifier = Modifier.width(75.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ShiftToggleButton(label = "Matí", isSelected = morning, onClick = { onChanged(!morning, afternoon) })
            ShiftToggleButton(label = "Tarda", isSelected = afternoon, onClick = { onChanged(morning, !afternoon) })
        }
    }
}

@Composable
fun ShiftToggleButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ButtonPrimary else Color(0xFFF1F5F9),
        modifier = Modifier.height(34.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else Color(0xFF64748B))
        }
    }
}

@Composable
fun TimeSelector(time: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = ButtonPrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(time, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
        }
    }
}

fun getShiftsFromDto(dto: DentistAvailabilityDTO, day: String): List<TimeSlot> {
    val list = mutableListOf<TimeSlot>()
    when (day) {
        "Monday" -> {
            if (dto.mondayMorningActive) list.add(TimeSlot(dto.mondayMorningStart ?: "09:00", dto.mondayMorningEnd ?: "14:00"))
            if (dto.mondayAfternoonActive) list.add(TimeSlot(dto.mondayAfternoonStart ?: "15:00", dto.mondayAfternoonEnd ?: "20:00"))
            if (dto.mondayEveningActive) list.add(TimeSlot(dto.mondayEveningStart ?: "20:00", dto.mondayEveningEnd ?: "22:00"))
        }
        "Tuesday" -> {
            if (dto.tuesdayMorningActive) list.add(TimeSlot(dto.tuesdayMorningStart ?: "09:00", dto.tuesdayMorningEnd ?: "14:00"))
            if (dto.tuesdayAfternoonActive) list.add(TimeSlot(dto.tuesdayAfternoonStart ?: "15:00", dto.tuesdayAfternoonEnd ?: "20:00"))
            if (dto.tuesdayEveningActive) list.add(TimeSlot(dto.tuesdayEveningStart ?: "20:00", dto.tuesdayEveningEnd ?: "22:00"))
        }
        "Wednesday" -> {
            if (dto.wednesdayMorningActive) list.add(TimeSlot(dto.wednesdayMorningStart ?: "09:00", dto.wednesdayMorningEnd ?: "14:00"))
            if (dto.wednesdayAfternoonActive) list.add(TimeSlot(dto.wednesdayAfternoonStart ?: "15:00", dto.wednesdayAfternoonEnd ?: "20:00"))
            if (dto.wednesdayEveningActive) list.add(TimeSlot(dto.wednesdayEveningStart ?: "20:00", dto.wednesdayEveningEnd ?: "22:00"))
        }
        "Thursday" -> {
            if (dto.thursdayMorningActive) list.add(TimeSlot(dto.thursdayMorningStart ?: "09:00", dto.thursdayMorningEnd ?: "14:00"))
            if (dto.thursdayAfternoonActive) list.add(TimeSlot(dto.thursdayAfternoonStart ?: "15:00", dto.thursdayAfternoonEnd ?: "20:00"))
            if (dto.thursdayEveningActive) list.add(TimeSlot(dto.thursdayEveningStart ?: "20:00", dto.thursdayEveningEnd ?: "22:00"))
        }
        "Friday" -> {
            if (dto.fridayMorningActive) list.add(TimeSlot(dto.fridayMorningStart ?: "09:00", dto.fridayMorningEnd ?: "14:00"))
            if (dto.fridayAfternoonActive) list.add(TimeSlot(dto.fridayAfternoonStart ?: "15:00", dto.fridayAfternoonEnd ?: "20:00"))
            if (dto.fridayEveningActive) list.add(TimeSlot(dto.fridayEveningStart ?: "20:00", dto.fridayEveningEnd ?: "22:00"))
        }
    }
    return list
}

fun mapShiftsToDto(dto: DentistAvailabilityDTO, day: String, shifts: List<TimeSlot>): DentistAvailabilityDTO {
    val morning = shifts.getOrNull(0)
    val afternoon = shifts.getOrNull(1)
    val evening = shifts.getOrNull(2)

    return when (day) {
        "Monday" -> dto.copy(
            mondayMorningActive = morning != null, mondayMorningStart = morning?.start ?: "09:00", mondayMorningEnd = morning?.end ?: "14:00",
            mondayAfternoonActive = afternoon != null, mondayAfternoonStart = afternoon?.start ?: "15:00", mondayAfternoonEnd = afternoon?.end ?: "20:00",
            mondayEveningActive = evening != null, mondayEveningStart = evening?.start ?: "20:00", mondayEveningEnd = evening?.end ?: "22:00"
        )
        "Tuesday" -> dto.copy(
            tuesdayMorningActive = morning != null, tuesdayMorningStart = morning?.start ?: "09:00", tuesdayMorningEnd = morning?.end ?: "14:00",
            tuesdayAfternoonActive = afternoon != null, tuesdayAfternoonStart = afternoon?.start ?: "15:00", tuesdayAfternoonEnd = afternoon?.end ?: "20:00",
            tuesdayEveningActive = evening != null, tuesdayEveningStart = evening?.start ?: "20:00", tuesdayEveningEnd = evening?.end ?: "22:00"
        )
        "Wednesday" -> dto.copy(
            wednesdayMorningActive = morning != null, wednesdayMorningStart = morning?.start ?: "09:00", wednesdayMorningEnd = morning?.end ?: "14:00",
            wednesdayAfternoonActive = afternoon != null, wednesdayAfternoonStart = afternoon?.start ?: "15:00", wednesdayAfternoonEnd = afternoon?.end ?: "20:00",
            wednesdayEveningActive = evening != null, wednesdayEveningStart = evening?.start ?: "20:00", wednesdayEveningEnd = evening?.end ?: "22:00"
        )
        "Thursday" -> dto.copy(
            thursdayMorningActive = morning != null, thursdayMorningStart = morning?.start ?: "09:00", thursdayMorningEnd = morning?.end ?: "14:00",
            thursdayAfternoonActive = afternoon != null, thursdayAfternoonStart = afternoon?.start ?: "15:00", thursdayAfternoonEnd = afternoon?.end ?: "20:00",
            thursdayEveningActive = evening != null, thursdayEveningStart = evening?.start ?: "20:00", thursdayEveningEnd = evening?.end ?: "22:00"
        )
        "Friday" -> dto.copy(
            fridayMorningActive = morning != null, fridayMorningStart = morning?.start ?: "09:00", fridayMorningEnd = morning?.end ?: "14:00",
            fridayAfternoonActive = afternoon != null, fridayAfternoonStart = afternoon?.start ?: "15:00", fridayAfternoonEnd = afternoon?.end ?: "20:00",
            fridayEveningActive = evening != null, fridayEveningStart = evening?.start ?: "20:00", fridayEveningEnd = evening?.end ?: "22:00"
        )
        else -> dto
    }
}