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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.user.User

// 1. Función actualizada: Ahora acepta "Any?" para evitar cualquier error de compilación
fun getStaffImage(userId: Long?, sexRaw: Any?, rolesRaw: Any?): Int {
    val id = userId ?: 0L

    // Convertimos los roles a texto de forma segura para comprobar si es doctor
    val rolesString = rolesRaw?.toString()?.uppercase() ?: ""
    val isDoctor = rolesString.contains("DOCTOR") || rolesString.contains("DENTIST")

    // Convertimos el sexo a texto de forma segura
    val sexStr = sexRaw?.toString()?.uppercase()

    return if (isDoctor) {
        when (sexStr) {
            "FEMALE", "DONA", "MUJER" -> {
                val femaleDoctors = listOf(R.drawable.doctor1, R.drawable.doctor2, R.drawable.doctor3, R.drawable.doctor4)
                femaleDoctors[(id % femaleDoctors.size).toInt()]
            }
            "MALE", "HOME", "HOMBRE" -> {
                val maleDoctors = listOf(R.drawable.doctor5, R.drawable.doctor6, R.drawable.doctor7, R.drawable.doctor8, R.drawable.doctor9)
                maleDoctors[(id % maleDoctors.size).toInt()]
            }
            else -> R.drawable.doctorincog
        }
    } else {
        when (sexStr) {
            "FEMALE", "DONA", "MUJER" -> {
                val femaleOptions = listOf(R.drawable.usuario1, R.drawable.usuario4, R.drawable.usuario5, R.drawable.usuario6, R.drawable.usuario7, R.drawable.usuario8, R.drawable.usuario11)
                femaleOptions[(id % femaleOptions.size).toInt()]
            }
            "MALE", "HOME", "HOMBRE" -> {
                val maleOptions = listOf(R.drawable.usuario2, R.drawable.usuario3, R.drawable.usuario9, R.drawable.usuario10, R.drawable.usuario12, R.drawable.usuario13, R.drawable.usuario20)
                maleOptions[(id % maleOptions.size).toInt()]
            }
            else -> R.drawable.incog
        }
    }
}

// 2. Componente StaffItem actualizado
@Composable
fun StaffItem(
    staff: User,
    onClick: (User) -> Unit
) {
    // Formatear los roles de forma segura (funciona tanto si es una Lista como si es un String simple)
    val displayRoles = when (val r = staff.roles) {
        is List<*> -> r.joinToString(", ") {
            it.toString().replace("ROLE_", "").lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        is String -> r.replace("ROLE_", "").lowercase().replaceFirstChar { char -> char.uppercase() }
        else -> ""
    }

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

            // Comprobamos si el usuario tiene un avatar en la Base de Datos
            if (!staff.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = staff.avatarUrl,
                    contentDescription = "Avatar de ${staff.name}",
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    // Al usar Any?, esto nunca más dará un error de "Type Mismatch"
                    painter = painterResource(id = getStaffImage(staff.id, staff.sex, staff.roles)),
                    contentDescription = "Avatar por defecto",
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${staff.name ?: ""} ${staff.surname ?: ""}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = displayRoles,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}