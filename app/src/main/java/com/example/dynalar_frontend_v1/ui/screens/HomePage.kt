package com.example.dynalar_frontend_v1.ui.screens

import android.R.attr.text
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Appointment
import com.example.dynalar_frontend_v1.ui.components.CardMenuButton
import com.example.dynalar_frontend_v1.ui.components.DayAppointmentsDialog
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.utils.changeLanguage
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.layout.ContentScale
import com.example.dynalar_frontend_v1.utils.changeLanguage
@Composable
fun HomePage(
    viewModel: AppointmentViewModel = viewModel(),
    onNavigateProfileUserProfile: () -> Unit,
    onNavigateListPacient: () -> Unit,
    onNavigateBoxCalendar: () -> Unit,
    onNavigateToAppointmentDetail: (Appointment) -> Unit,
    onNavigateBoxMaterials: () -> Unit,
    onNavigateToPatientProfile: (Long) -> Unit,
    onLanguageChange: (String) -> Unit = {} // ← NUEVO

) {
    LaunchedEffect(Unit) {
        viewModel.fetchToday()
    }

    var selectedDateForDialog by remember { mutableStateOf<LocalDate?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPagina)
    ) {
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .heightIn(min = screenHeight)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Header_HomePage(
                onNavigateProfileUserProfile = onNavigateProfileUserProfile,
                onLanguageChange = onLanguageChange
            )
            Spacer(modifier = Modifier.height(40.dp))

            val uiState = viewModel.uiStateToday
            val today = LocalDate.now()
            val nowTime = LocalTime.now()

            var citasHoyCount = 0
            var nextAppointments = emptyList<Appointment>()

            if (uiState is InterfaceGlobal.Success) {
                val todayAppointments = uiState.data
                citasHoyCount = todayAppointments.size

                if (todayAppointments.isNotEmpty()) {

                    val groupedByTime = todayAppointments.groupBy { appt ->
                        appt.startTime?.replace("T", " ")?.split(" ")?.lastOrNull()?.take(5) ?: "23:59"
                    }


                    val bestGroup = groupedByTime.minByOrNull { (timeStr, _) ->
                        try {
                            val parts = timeStr.split(":")
                            val apptMinutes = parts[0].toInt() * 60 + parts[1].toInt()
                            val currentMinutes = nowTime.hour * 60 + nowTime.minute

                            if (apptMinutes >= currentMinutes) {
                                apptMinutes - currentMinutes
                            } else {
                                10000 + (currentMinutes - apptMinutes)
                            }
                        } catch (e: Exception) {
                            99999
                        }
                    }
                    nextAppointments = bestGroup?.value ?: emptyList()
                }
            }

            GreetingSection(citasHoy = citasHoyCount)

            Spacer(modifier = Modifier.height(8.dp))

            CalendarHomepage(
                viewModel = viewModel,
                onDayClick = { date ->
                    viewModel.fetchDayDetails(date)
                    selectedDateForDialog = date
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Buttons_HomePage(
                onNavigateListPacient = onNavigateListPacient,
                onNavigateBoxCalendar = onNavigateBoxCalendar,
                onNavigateBoxMaterials = onNavigateBoxMaterials
            )

            Spacer(modifier = Modifier.height(40.dp))

            NextAppointmentSection(
                isLoading = uiState is InterfaceGlobal.Loading,
                nextAppointments = nextAppointments,
                onAppointmentClick = { appointment ->
                    onNavigateToAppointmentDetail(appointment)
                }
            )
        }
    }

    if (selectedDateForDialog != null) {
        val detailUiState = viewModel.uiStateCalendar
        val appointments = if (detailUiState is InterfaceGlobal.Success) detailUiState.data else emptyList()

        DayAppointmentsDialog(
            date = selectedDateForDialog!!,
            appointments = appointments,
            isLoading = detailUiState is InterfaceGlobal.Loading,
            onDismiss = {
                selectedDateForDialog = null
            },
            onAppointmentClick = { appointment ->
                onNavigateToAppointmentDetail(appointment)
            }
        )
    }
}

@Composable
fun GreetingSection(citasHoy: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        val text = if (citasHoy == 1) {
            stringResource(id = R.string.home_citas_today_one)
        } else {
            stringResource(id = R.string.home_citas_today_many, citasHoy)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
fun NextAppointmentSection(
    isLoading: Boolean,
    nextAppointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(id = R.string.home_next_appointment_title),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ButtonPrimary)
            }

        } else if (nextAppointments.isNotEmpty()) {

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {

                items(nextAppointments) { appointment ->

                    NextAppointmentCardItem(
                        appointment = appointment,
                        modifier = Modifier.fillParentMaxWidth(
                            if (nextAppointments.size > 1) 0.85f else 1f
                        ),
                        onAppointmentClick = onAppointmentClick
                    )
                }
            }

        } else {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {

                        Text(
                            text = stringResource(id = R.string.home_no_more_appointments),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )

                        Text(
                            text = stringResource(id = R.string.home_completed_workday),
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun NextAppointmentCardItem(
    appointment: Appointment,
    modifier: Modifier = Modifier,
    onAppointmentClick: (Appointment) -> Unit
) {

    val timeStr = appointment.startTime
        ?.split("T", " ")
        ?.lastOrNull()
        ?.substring(0, 5) ?: "--:--"

    val defaultPatientName = stringResource(id = R.string.home_unknown_patient)

    val patientName =
        "${appointment.patient?.name ?: defaultPatientName} ${appointment.patient?.lastName ?: ""}".trim()

    val treatmentName =
        appointment.treatment?.name ?: stringResource(id = R.string.home_unspecified)

    val boxInfo =
        appointment.box?.number?.let { "Box $it" } ?: ""
    val allergies =
        appointment.patient?.medicalRecord?.allergies

    val infectiousDeceases =
        appointment.patient?.medicalRecord?.infectiousDeceases

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onAppointmentClick(appointment)
                }
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD)),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = timeStr,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5BB2),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = patientName.ifEmpty { stringResource(id = R.string.home_unknown_patient) },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (boxInfo.isNotEmpty())
                        "$boxInfo | $treatmentName"
                    else
                        treatmentName,

                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!allergies.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.patient_allergies_prefix, allergies),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF57C00),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!infectiousDeceases.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.patient_infectious_prefix, infectiousDeceases),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = stringResource(id = R.string.home_view_summary),
                tint = Color(0xFF1A5BB2),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
@Composable
fun Header_HomePage(
    onNavigateProfileUserProfile: () -> Unit,
    onLanguageChange: (String) -> Unit
) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val currentAvatarResId = prefs.getInt("user_avatar", R.drawable.avatar_color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + nombre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onNavigateProfileUserProfile() }
                .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Image(
                painter = painterResource(id = currentAvatarResId),
                contentDescription = stringResource(id = R.string.home_my_profile),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.home_my_profile),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(id = R.string.home_user),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(id = R.string.home_view_profile),
                tint = Color.Gray
            )
        }

        // Selector de idioma
        HomeLanguageSelector(onLanguageChange = onLanguageChange)
    }
}
@Composable
fun HomeLanguageSelector(onLanguageChange: (String) -> Unit) {
    val context = LocalContext.current


    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var currentLangCode by remember {
        mutableStateOf(prefs.getString("language", "ca") ?: "ca")
    }

    val languages = listOf("ca" to "CA", "es" to "ES", "en" to "EN")
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Idioma", tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = currentLangCode.uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            name,
                            fontWeight = FontWeight.Medium,
                            color = if (code == currentLangCode) Color(0xFF537895) else Color.Black
                        )
                    },
                    onClick = {
                        expanded = false
                        currentLangCode = code
                        onLanguageChange(code)
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHomepage(viewModel: AppointmentViewModel, onDayClick: (LocalDate) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    var showDatePicker by remember { mutableStateOf(false) }

    // 1. Obtener el Locale actual del sistema dinámicamente
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales[0]

    LaunchedEffect(currentMonth) {
        val startOfMonth = currentMonth.atDay(1).atStartOfDay()
        val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        viewModel.fetchSummary(startOfMonth, endOfMonth)
    }
    val uiState = viewModel.uiStateSummary

    val summaryByDate = remember(uiState) {
        if (uiState is InterfaceGlobal.Success) {
            uiState.data.associateBy {
                try {
                    val cleanDate = it.date.take(10)
                    LocalDate.parse(cleanDate)
                } catch (e: Exception) {
                    null
                }
            }
        } else emptyMap()
    }

    val weekDays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
    val firstDay = currentMonth.atDay(1)
    val offset = firstDay.dayOfWeek.value - 1
    val daysInMonth = currentMonth.lengthOfMonth()


    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    val headerText = currentMonth.format(formatter).replaceFirstChar { it.uppercase() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 50) currentMonth = currentMonth.minusMonths(1)
                        else if (offsetX < -50) currentMonth = currentMonth.plusMonths(1)
                        offsetX = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount
                }
            },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(headerText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2C2C))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("▾", fontSize = 12.sp, color = Color(0xFF888888))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(id = R.string.home_prev_month), tint = Color(0xFF555555))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(id = R.string.home_next_month), tint = Color(0xFF555555))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(

                        text = day.getDisplayName(TextStyle.NARROW, Locale.ENGLISH),
                        modifier = Modifier.weight(1f).height(38.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth().height(38.dp)) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val day = cellIndex - offset + 1
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (day in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(day)
                                val isToday = cellDate == today
                                val summary = summaryByDate[cellDate]
                                val hasAppointment = summary?.hasAppointments == true
                                val hasInfectious = summary?.hasinfeciousPatient == true
                                val circleColor = if (hasInfectious) Color(0xFFD32F2F) else ButtonPrimary

                                Box(
                                    modifier = Modifier.size(30.dp).clickable(enabled = hasAppointment) { onDayClick(cellDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasAppointment) Surface(shape = CircleShape, color = circleColor, modifier = Modifier.fillMaxSize()) {}
                                    else if (isToday) Surface(shape = CircleShape, color = Color.Transparent, border = BorderStroke(1.5.dp, Color(0xFF2C2C2C)), modifier = Modifier.fillMaxSize()) {}

                                    Text(
                                        day.toString(),
                                        fontSize = 14.sp,
                                        color = if (hasAppointment) Color.White else Color(0xFF2C2C2C),
                                        fontWeight = if (hasAppointment || isToday) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMonth.atDay(1).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                        currentMonth = YearMonth.from(selectedDate)
                    }
                    showDatePicker = false
                }) { Text(stringResource(id = R.string.btn_accept), color = ButtonPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(id = R.string.btn_cancel), color = ButtonPrimary) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}
@Composable
fun Buttons_HomePage(modifier: Modifier = Modifier, onNavigateListPacient: () -> Unit, onNavigateBoxCalendar: () -> Unit, onNavigateBoxMaterials: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardMenuButton(
                icon = Icons.Default.Person,
                title = stringResource(id = R.string.home_btn_patients),
                onClick = onNavigateListPacient,
                modifier = Modifier.weight(1f)
            )
            CardMenuButton(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(id = R.string.home_btn_agenda),
                onClick = onNavigateBoxCalendar,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardMenuButton(
                icon = Icons.Default.Inventory,
                title = stringResource(id = R.string.home_btn_management),
                onClick = onNavigateBoxMaterials,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}