package com.example.dynalar_frontend_v1.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.staff.AbsenceEvent
import com.example.dynalar_frontend_v1.model.staff.AbsenceType
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.StaffControlViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun ClockInScreen(
    viewModel: StaffControlViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val isSuperAdmin = sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN")
    val isOwner = sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER")
    val canEditTime = isSuperAdmin || isOwner
    val currentUserId = sessionManager.getUserId()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Using stringResource for the default selection
    val defaultWorkDayType = stringResource(id = R.string.day_type_work)
    var selectedDayType by remember { mutableStateOf(defaultWorkDayType) }

    var showTimeDialogFor by remember { mutableStateOf<String?>(null) }
    var initialDialogTime by remember { mutableStateOf<String?>(null) }

    var localCheckInOverride by remember(selectedDate) { mutableStateOf<String?>(null) }
    var localCheckOutOverride by remember(selectedDate) { mutableStateOf<String?>(null) }

    var localAbsences by remember { mutableStateOf<List<AbsenceEvent>>(emptyList()) }
    var localWorkedDays by remember { mutableStateOf(setOf<LocalDate>()) }

    val loadingText = stringResource(id = R.string.loading_staff_info) // e.g., "Cargando..."
    val defaultRole = stringResource(id = R.string.default_clinic_name) // e.g., "Clínica Dynalar"

    var backendCheckIn by remember { mutableStateOf<String?>(null) }
    var backendCheckOut by remember { mutableStateOf<String?>(null) }
    var staffName by remember { mutableStateOf(loadingText) }
    var role by remember { mutableStateOf(defaultRole) }

    LaunchedEffect(currentMonth) {
        viewModel.fetchMonthlyAbsences(currentMonth)
    }

    LaunchedEffect(viewModel.uiStateAttendance) {
        if (viewModel.uiStateAttendance is InterfaceGlobal.Success) {
            val attendanceList = (viewModel.uiStateAttendance as InterfaceGlobal.Success).data
            val myAttendance = attendanceList.find { it.id.toString() == currentUserId.toString() } ?: attendanceList.firstOrNull()

            backendCheckIn = myAttendance?.checkInTime?.toString()?.takeIf { it != "null" && it.isNotBlank() }?.take(5)
            backendCheckOut = myAttendance?.checkOutTime?.toString()?.takeIf { it != "null" && it.isNotBlank() }?.take(5)
            if (myAttendance?.staffName != null) staffName = myAttendance.staffName
            if (myAttendance?.role != null) role = myAttendance.role
        }
    }

    LaunchedEffect(selectedDate) {
        localCheckInOverride = null
        localCheckOutOverride = null
        backendCheckIn = null
        backendCheckOut = null
        viewModel.fetchDailyAttendance(selectedDate)
    }

    LaunchedEffect(backendCheckIn, selectedDate) {
        if (backendCheckIn != null) {
            localWorkedDays = localWorkedDays + selectedDate
        } else {
            localWorkedDays = localWorkedDays - selectedDate
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                Spacer(modifier = Modifier.height(27.dp))
                CustomTopBar(title = stringResource(R.string.clock_in_title), onNavigateBack = onNavigateBack)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val backendAbsences = if (viewModel.uiStateAbsences is InterfaceGlobal.Success) {
                (viewModel.uiStateAbsences as InterfaceGlobal.Success).data.map { dto ->
                    AbsenceEvent(
                        id = dto.id,
                        title = dto.title,
                        staffName = dto.staffName,
                        type = try { AbsenceType.valueOf(dto.type.uppercase()) } catch (e: Exception) { AbsenceType.HOLIDAY },
                        startDate = LocalDate.parse(dto.startDate),
                        endDate = LocalDate.parse(dto.endDate)
                    )
                }
            } else emptyList()

            val allEvents = backendAbsences + localAbsences

            ClockCalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                events = allEvents,
                workedDays = localWorkedDays,
                onMonthChange = { currentMonth = it },
                onDateSelect = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            DayTypeDropdown(
                selectedType = selectedDayType,
                onTypeSelected = { selectedDayType = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            val noTimeDefault = "09:00" // Or consider stringResource(R.string.default_start_time)
            val displayCheckIn = localCheckInOverride ?: backendCheckIn ?: noTimeDefault
            val displayCheckOut = localCheckOutOverride ?: backendCheckOut ?: "18:00"

            val isCheckInRegistered = backendCheckIn != null
            val isCheckOutRegistered = backendCheckOut != null

            val hasUnsavedIn = localCheckInOverride != null && localCheckInOverride != backendCheckIn
            val hasUnsavedOut = localCheckOutOverride != null && localCheckOutOverride != backendCheckOut
            val hasAnyUnsaved = hasUnsavedIn || hasUnsavedOut

            // Comparing against the localized string for "Trabajo"
            if (selectedDayType == stringResource(id = R.string.day_type_work)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = staffName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        if (role.isNotEmpty()) {
                            Text(text = role.uppercase(), fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(stringResource(id = R.string.working_day_header), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 16.dp))

                        TimeRow(
                            label = stringResource(id = R.string.check_in_time_label),
                            timeStr = displayCheckIn,
                            hasUnsavedChanges = hasUnsavedIn,
                            canEdit = canEditTime,
                            onEditClick = {
                                initialDialogTime = displayCheckIn
                                showTimeDialogFor = "IN"
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TimeRow(
                            label = stringResource(id = R.string.check_out_time_label),
                            timeStr = displayCheckOut,
                            hasUnsavedChanges = hasUnsavedOut,
                            canEdit = canEditTime,
                            onEditClick = {
                                initialDialogTime = displayCheckOut
                                showTimeDialogFor = "OUT"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isCompleted = isCheckInRegistered && isCheckOutRegistered && !hasAnyUnsaved

                val buttonText = when {
                    hasAnyUnsaved -> stringResource(id = R.string.btn_save_changes)
                    !isCheckInRegistered -> stringResource(id = R.string.btn_register_work)
                    !isCheckOutRegistered -> stringResource(id = R.string.btn_register_out)
                    else -> stringResource(id = R.string.status_day_completed)
                }

                val buttonColor = when {
                    hasAnyUnsaved -> ButtonPrimary
                    !isCheckInRegistered -> Color(0xFF2E7D32)
                    !isCheckOutRegistered -> Color(0xFFD32F2F)
                    else -> Color(0xFFE2E8F0)
                }

                Button(
                    onClick = {
                        when {
                            hasAnyUnsaved -> {
                                if (hasUnsavedIn && hasUnsavedOut) {
                                    viewModel.registerClock("IN", displayCheckIn) {
                                        viewModel.registerClock("OUT", displayCheckOut) {
                                            Toast.makeText(context, context.getString(R.string.msg_changes_saved), Toast.LENGTH_SHORT).show()
                                            localCheckInOverride = null
                                            localCheckOutOverride = null
                                            viewModel.fetchDailyAttendance(selectedDate)
                                        }
                                    }
                                } else if (hasUnsavedIn) {
                                    viewModel.registerClock("IN", displayCheckIn) {
                                        Toast.makeText(context, context.getString(R.string.msg_in_saved), Toast.LENGTH_SHORT).show()
                                        localCheckInOverride = null
                                        viewModel.fetchDailyAttendance(selectedDate)
                                    }
                                } else if (hasUnsavedOut) {
                                    viewModel.registerClock("OUT", displayCheckOut) {
                                        Toast.makeText(context, context.getString(R.string.msg_out_saved), Toast.LENGTH_SHORT).show()
                                        localCheckOutOverride = null
                                        viewModel.fetchDailyAttendance(selectedDate)
                                    }
                                }
                            }
                            !isCheckInRegistered -> {
                                viewModel.registerClock("IN", displayCheckIn) { success ->
                                    if (success) {
                                        Toast.makeText(context, context.getString(R.string.msg_clock_in_success), Toast.LENGTH_SHORT).show()
                                        viewModel.fetchDailyAttendance(selectedDate)
                                    }
                                }
                            }
                            !isCheckOutRegistered -> {
                                viewModel.registerClock("OUT", displayCheckOut) { success ->
                                    if (success) {
                                        Toast.makeText(context, context.getString(R.string.msg_clock_out_success), Toast.LENGTH_SHORT).show()
                                        viewModel.fetchDailyAttendance(selectedDate)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    enabled = !isCompleted
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

            } else {

                val typeVacation = stringResource(id = R.string.day_type_vacation)
                val typeSick = stringResource(id = R.string.day_type_sick)

                val (absTitle, absColor, absType) = when (selectedDayType) {
                    typeVacation -> Triple(stringResource(id = R.string.btn_register_vacation), Color(0xFFFFB300), AbsenceType.VACATION)
                    typeSick -> Triple(stringResource(id = R.string.btn_register_sick), Color(0xFF039BE5), AbsenceType.SICK_LEAVE)
                    else -> Triple(stringResource(id = R.string.btn_register_holiday), Color(0xFFE53935), AbsenceType.HOLIDAY)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(id = R.string.selected_day_for), color = Color.Gray, fontSize = 15.sp)
                        Text(
                            text = selectedDayType.uppercase(),
                            color = absColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )

                        Button(
                            onClick = {
                                localAbsences = localAbsences + AbsenceEvent(
                                    id = System.currentTimeMillis(),
                                    title = selectedDayType,
                                    staffName = staffName,
                                    type = absType,
                                    startDate = selectedDate,
                                    endDate = selectedDate
                                )
                                // Using formatted string resource for the toast
                                val toastMsg = context.getString(R.string.msg_calendar_painted, selectedDayType)
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = absColor)
                        ) {
                            Text(absTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showTimeDialogFor != null) {
        CustomTimePickerDialog(
            initialTime = initialDialogTime,
            onDismiss = { showTimeDialogFor = null },
            onConfirm = { formattedTime ->
                if (showTimeDialogFor == "IN") {
                    localCheckInOverride = formattedTime
                } else if (showTimeDialogFor == "OUT") {
                    localCheckOutOverride = formattedTime
                }
                showTimeDialogFor = null
            }
        )
    }
}

@Composable
fun TimeRow(
    label: String,
    timeStr: String,
    hasUnsavedChanges: Boolean,
    canEdit: Boolean,
    onEditClick: () -> Unit
) {
    val displayColor = if (hasUnsavedChanges) Color(0xFFF57C00) else Color(0xFF1E293B)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = timeStr,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = displayColor
            )

            if (canEdit) {
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.content_desc_edit), tint = ButtonPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ClockCalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<AbsenceEvent>,
    workedDays: Set<LocalDate>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val headerText = currentMonth.format(formatter).replaceFirstChar { it.uppercase() }

    val daysInMonth = currentMonth.lengthOfMonth()
    val offset = currentMonth.atDay(1).dayOfWeek.value - 1
    val weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(id = R.string.content_desc_prev_month), tint = ButtonPrimary)
                }
                Text(text = headerText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(id = R.string.content_desc_next_month), tint = ButtonPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(text = day.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val rows = (offset + daysInMonth + 6) / 7

            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth().height(42.dp)) {
                    repeat(7) { col ->
                        val day = row * 7 + col - offset + 1

                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (day in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(day)
                                val isSelected = cellDate == selectedDate
                                val dayEvents = events.filter { !cellDate.isBefore(it.startDate) && !cellDate.isAfter(it.endDate) }

                                val bgColor = when {
                                    dayEvents.any { it.type == AbsenceType.VACATION } -> Color(0xFFFFB300)
                                    dayEvents.any { it.type == AbsenceType.HOLIDAY } -> Color(0xFFE53935)
                                    dayEvents.any { it.type == AbsenceType.SICK_LEAVE } -> Color(0xFF039BE5)
                                    workedDays.contains(cellDate) -> Color(0xFF4CAF50)
                                    isSelected && dayEvents.isEmpty() -> ButtonPrimary
                                    else -> Color.Transparent
                                }

                                val textColor = if (bgColor != Color.Transparent) Color.White else Color(0xFF1E293B)
                                val border = if (isSelected && bgColor != ButtonPrimary) BorderStroke(2.dp, ButtonPrimary) else null

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .then(if (border != null) Modifier.background(Color.Transparent, CircleShape) else Modifier)
                                        .clickable { onDateSelect(cellDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        border = border,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = day.toString(),
                                                fontSize = 15.sp,
                                                color = textColor,
                                                fontWeight = if (bgColor != Color.Transparent || isSelected) FontWeight.Bold else FontWeight.Normal
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
}

@Composable
fun DayTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Using string resources for dropdown options
    val options = listOf(
        stringResource(id = R.string.day_type_work),
        stringResource(id = R.string.day_type_vacation),
        stringResource(id = R.string.day_type_sick),
        stringResource(id = R.string.day_type_holiday)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(id = R.string.day_type_header), fontSize = 12.sp, color = Color.Gray)
                    Text(selectedType, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.content_desc_open), tint = Color.Gray)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White).fillMaxWidth(0.85f)) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 15.sp, color = Color(0xFF1E293B)) },
                    onClick = { onTypeSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun CustomTimePickerDialog(
    initialTime: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var hourText by remember { mutableStateOf(String.format("%02d", initialTime?.substringBefore(":")?.toIntOrNull() ?: LocalTime.now().hour)) }
    var minuteText by remember { mutableStateOf(String.format("%02d", initialTime?.substringAfter(":")?.toIntOrNull() ?: LocalTime.now().minute)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(id = R.string.dialog_edit_time), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        val h = hourText.toIntOrNull() ?: 0
                        hourText = String.format("%02d", if (h < 23) h + 1 else 0)
                    }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(id = R.string.content_desc_up_hour), modifier = Modifier.size(32.dp)) }

                    BasicTextField(
                        value = hourText,
                        onValueChange = { newValue ->
                            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                                val intVal = newValue.toIntOrNull()
                                hourText = if (intVal == null || intVal <= 23) newValue else "23"
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = ButtonPrimary, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(60.dp),
                        singleLine = true
                    )

                    IconButton(onClick = {
                        val h = hourText.toIntOrNull() ?: 0
                        hourText = String.format("%02d", if (h > 0) h - 1 else 23)
                    }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.content_desc_down_hour), modifier = Modifier.size(32.dp)) }
                }

                Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-4).dp), color = Color(0xFF1E293B))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        val m = minuteText.toIntOrNull() ?: 0
                        minuteText = String.format("%02d", if (m < 59) m + 1 else 0)
                    }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(id = R.string.content_desc_up_min), modifier = Modifier.size(32.dp)) }

                    BasicTextField(
                        value = minuteText,
                        onValueChange = { newValue ->
                            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                                val intVal = newValue.toIntOrNull()
                                minuteText = if (intVal == null || intVal <= 59) newValue else "59"
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = ButtonPrimary, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(60.dp),
                        singleLine = true
                    )

                    IconButton(onClick = {
                        val m = minuteText.toIntOrNull() ?: 0
                        minuteText = String.format("%02d", if (m > 0) m - 1 else 59)
                    }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.content_desc_down_min), modifier = Modifier.size(32.dp)) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hourText.toIntOrNull() ?: 0
                    val m = minuteText.toIntOrNull() ?: 0
                    onConfirm(String.format("%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59)))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                shape = RoundedCornerShape(8.dp)
            ) { Text(stringResource(R.string.btn_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = Color.Gray) }
        }
    )
}