import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.getPatientImage
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.TextoPrincipal

@Composable
fun PatientHeaderSection(patient: Patient) {
    PatientHeaderSectionAppBase(patient = patient, onClick = null, onDelete = null)
}

@Composable
fun PatientHeaderSectionApp(
    patient: Patient,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onGoToProfile: (() -> Unit)? = null
) {
    PatientHeaderSectionAppBase(patient = patient, onClick = onClick, onDelete = onDelete, onGoToProfile = onGoToProfile)
}

@Composable
private fun PatientHeaderSectionAppBase(
    patient: Patient,
    onClick: (() -> Unit)?,
    onDelete: (() -> Unit)? = null,
    onGoToProfile: (() -> Unit)? = null
) {
    val allergies = patient.medicalRecord?.allergies
    val infectiousDeceases = patient.medicalRecord?.infectiousDeceases

    val hasInfections = !infectiousDeceases.isNullOrBlank()
    val hasAllergies = !allergies.isNullOrBlank()

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Eliminar pacient",
            message = "Estàs segur que vols eliminar aquest pacient? Aquesta acció no es pot desfer.",
            onConfirm = {
                showDeleteDialog = false
                onDelete?.invoke()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = false,
                            ambientColor = ButtonPrimary.copy(alpha = 0.2f),
                            spotColor = ButtonPrimary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Image(
                        painter = painterResource(id = getPatientImage(patient.id ?: 0, patient.sex)),
                        contentDescription = "Foto del pacient",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${patient.name ?: ""} ${patient.lastName ?: ""}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal,
                        maxLines = 2,
                        modifier = Modifier.padding(end = if (onDelete != null) 32.dp else 0.dp)
                    )

                    Surface(
                        color = ButtonPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DNI: ${patient.dni ?: "No registrat"}",
                            fontSize = 13.sp,
                            color = ButtonPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Telf: ${patient.phone ?: "Sense telèfon"}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Surface(
                        color = if (hasInfections) Color.Red.copy(alpha = 0.1f) else Color(0xFF388E3C).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (hasInfections) Color.Red.copy(alpha = 0.3f) else Color(0xFF388E3C).copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (hasInfections) Icons.Default.Coronavirus else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasInfections) Color.Red else Color(0xFF388E3C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasInfections) infectiousDeceases!! else "Cap infecció coneguda",
                                color = if (hasInfections) Color.Red else Color(0xFF388E3C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = if (hasAllergies) Color(0xFFE65100).copy(alpha = 0.1f) else Color(0xFF388E3C).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (hasAllergies) Color(0xFFE65100).copy(alpha = 0.3f) else Color(0xFF388E3C).copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (hasAllergies) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasAllergies) Color(0xFFE65100) else Color(0xFF388E3C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasAllergies) "Al·lèrgic: $allergies" else "Sense al·lèrgies",
                                color = if (hasAllergies) Color(0xFFE65100) else Color(0xFF388E3C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (onGoToProfile != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, end = 10.dp)
                        ) {
                            Text(
                                text = "Fitxa del pacient",
                                color = ButtonPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = ButtonPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (onDelete != null) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar pacient",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}