package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar

@Composable
fun StaffControlHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToClockIn: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF9FAFB))) {
                Spacer(modifier = Modifier.height(27.dp))
                CustomTopBar(
                    title = stringResource(R.string.staff_control_title),
                    onNavigateBack = onNavigateBack
                )
            }
        }
    ) { paddingValues ->
        // Al usar Arrangement.Center, todo el bloque de tarjetas se quedará en el medio de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 24.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Tarjeta para Fichar
            StaffMenuCard(
                icon = Icons.Default.Fingerprint,
                title = stringResource(R.string.menu_clock_in_title),
                subtitle = stringResource(R.string.menu_clock_in_subtitle),
                onClick = onNavigateToClockIn
            )

            // 2. Tarjeta de Registro Diario
            StaffMenuCard(
                icon = Icons.Default.AccessTime,
                title = stringResource(R.string.menu_daily_attendance_title),
                subtitle = stringResource(R.string.menu_daily_attendance_subtitle),
                onClick = onNavigateToDaily
            )

            // 3. Tarjeta de Calendario
            StaffMenuCard(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(R.string.menu_absence_calendar_title),
                subtitle = stringResource(R.string.menu_absence_calendar_subtitle),
                onClick = onNavigateToCalendar
            )
        }
    }
}

// Componente visual con el tamaño original (sin estirarse)
@Composable
fun StaffMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp), // Tamaño original
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadro azul claro para el icono
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEBF4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}