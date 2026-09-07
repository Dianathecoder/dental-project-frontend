package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.patient.Sex
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.getPatientImage

fun getStaffImage(userId: Long?, roles: List<String>, sex: Any?): Int {
    val id = userId ?: 0L
    val upperRoles = roles.map { it.uppercase() }
    val isDoctor = upperRoles.any { it.contains("DOCTOR") || it.contains("DENTIST") }

    val sexEnum = when (sex) {
        is Sex -> sex
        is String -> try { Sex.valueOf(sex.uppercase()) } catch (e: Exception) { null }
        else -> null
    }

    return if (isDoctor) {
        when (sexEnum) {
            Sex.FEMALE -> {
                val femaleDoctorOptions = listOf(
                    R.drawable.doctor1,
                    R.drawable.doctor3,
                    R.drawable.doctor5,
                    R.drawable.doctor7,
                    R.drawable.doctor9
                )
                femaleDoctorOptions[(id % femaleDoctorOptions.size).toInt()]
            }
            Sex.MALE -> {
                val maleDoctorOptions = listOf(
                    R.drawable.doctor2,
                    R.drawable.doctor4,
                    R.drawable.doctor6,
                    R.drawable.doctor8
                )
                maleDoctorOptions[(id % maleDoctorOptions.size).toInt()]
            }
            Sex.OTHER, null -> R.drawable.doctorincog
        }
    } else {
        getPatientImage(userId, sexEnum)
    }
}

@Composable
fun StaffItem(
    staff: User,
    onClick: (User) -> Unit,
    isClockedIn: Boolean? = false
) {
    val roles = staff.roles.map { it.uppercase() }

    val roleLabel = when {
        roles.any { it.contains("SUPERADMIN") } -> "SuperAdmin"
        roles.any { it.contains("OWNER") } -> "Propietari/a"
        roles.any { it.contains("ADMIN") } -> "Administrador/a"
        roles.any { it.contains("DOCTOR") || it.contains("DENTIST") } -> "Doctor/a"
        roles.any { it.contains("AUXILIAR") } -> "Auxiliar"
        else -> "Personal"
    }

    val roleColor = when {
        roles.any { it.contains("SUPERADMIN") || it.contains("OWNER") } -> Color(0xFF7B1FA2)
        roles.any { it.contains("ADMIN") } -> Color(0xFF1976D2)
        roles.any { it.contains("DOCTOR") || it.contains("DENTIST") } -> Color(0xFF388E3C)
        else -> Color(0xFFF57C00)
    }

    val clockStatus = remember(isClockedIn) { evaluateClockInStatus(isClockedIn) }
    val avatarRes = getStaffImage(staff.id, staff.roles, staff.sex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick(staff) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "Staff Avatar",
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${staff.name ?: ""} ${staff.surname ?: ""}".trim(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = roleLabel,
                        color = roleColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Indicador de Estado de Fichaje
                when (clockStatus) {
                    ClockInStatus.CLOCKED_IN -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF388E3C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fitxat",
                                color = Color(0xFF388E3C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    ClockInStatus.LATE_WARNING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sense fitxar (+1h)",
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ClockInStatus.PENDING -> {
                        val email = staff.email
                        if (!email.isNullOrBlank()) {
                            Text(
                                text = email,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}