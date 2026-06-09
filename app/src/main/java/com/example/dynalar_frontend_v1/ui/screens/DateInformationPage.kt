package com.example.dynalar_frontend_v1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.patient.MedicalRecord
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.SignatureView
import com.example.dynalar_frontend_v1.ui.components.ValidationAndSignatureDialog

val ButtonPrimary = Color(0xFF4A6D8C)
val FondoPagina = Color(0xFFF7F6F4)


enum class SignatureType {
    ANESTHESIA, HISTORY
}

@Composable
fun DateInformationPage(
    patient: Patient,
    onBackClick: () -> Unit,
    onUpdatePatient: (Patient) -> Unit
) {

    var activeSignatureType by remember { mutableStateOf<SignatureType?>(null) }
    val context = LocalContext.current

    if (activeSignatureType != null) {
        val isAnesthesia = activeSignatureType == SignatureType.ANESTHESIA

        ValidationAndSignatureDialog(
            title = if (isAnesthesia) "Consentiment Anestèsia" else "Confirmació d'Historial",
            consentTitle = if (isAnesthesia) "Consentiment Anestèsia:" else "Confirmació de dades:",
            consentText = if (isAnesthesia) null else "El pacient confirma que les dades de l'historial mèdic, malalties i al·lèrgies revisades són correctes.",
            infectiousDeceases = patient.medicalRecord?.infectiousDeceases,
            allergies = patient.medicalRecord?.allergies,
            isOptional = isAnesthesia, // Si es anestesia es opcional, si es historial es obligatorio
            onConfirm = { signatureBase64 ->
                val currentRecord = patient.medicalRecord ?: MedicalRecord()
                val updatedRecord = if (isAnesthesia) {
                    currentRecord.copy(signatureBase64 = signatureBase64)
                } else {
                    currentRecord.copy(signatureConfirmation = signatureBase64)
                }

                val updatedPatient = if (isAnesthesia) {
                    patient.copy(
                        medicalRecord = updatedRecord,
                        anesthesiaConsent = !signatureBase64.isNullOrBlank()
                    )
                } else {
                    patient.copy(medicalRecord = updatedRecord)
                }

                onUpdatePatient(updatedPatient)
                activeSignatureType = null
                Toast.makeText(context, "Signatura guardada correctament", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { activeSignatureType = null }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(FondoPagina)) {
                Spacer(modifier = Modifier.height(27.dp))
                CustomTopBar(
                    title = "Historial Mèdic",
                    onNavigateBack = onBackClick
                )
            }
        },
        containerColor = FondoPagina
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCardReadOnly(label = "Historial Familiar", value = patient.medicalRecord?.familyHistory, icon = Icons.Default.FamilyRestroom)
                InfoCardReadOnly(label = "Malalties i Condicions", value = patient.medicalRecord?.deceases, icon = Icons.Default.MedicalServices)
                InfoCardReadOnly(label = "Medicació", value = patient.medicalRecord?.medication, icon = Icons.Default.Medication)
                InfoCardReadOnly(label = "Al·lèrgies", value = patient.medicalRecord?.allergies, icon = Icons.Default.Warning)
                InfoCardReadOnly(label = "Malalties Infeccioses", value = patient.medicalRecord?.infectiousDeceases, icon = Icons.Default.Coronavirus)

                Spacer(modifier = Modifier.height(12.dp))


                SignatureSection(
                    title = "Consentiment Anestèsia",
                    signatureBase64 = patient.medicalRecord?.signatureBase64,
                    onRequestSignature = { activeSignatureType = SignatureType.ANESTHESIA }
                )


                SignatureSection(
                    title = "Confirmació d'Historial",
                    signatureBase64 = patient.medicalRecord?.signatureConfirmation,
                    onRequestSignature = { activeSignatureType = SignatureType.HISTORY }
                )
            }
        }
    }
}


@Composable
fun SignatureSection(
    title: String,
    signatureBase64: String?,
    onRequestSignature: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SolidColor(Color.LightGray.copy(alpha = 0.4f))),
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
        ) {

            if (!signatureBase64.isNullOrBlank()) {

                Box(modifier = Modifier.padding(16.dp)) {
                    SignatureView(signatureBase64 = signatureBase64)
                }
            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onRequestSignature,
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Afegir Signatura")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCardReadOnly(
    label: String,
    value: String?,
    icon: ImageVector
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ButtonPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SolidColor(Color.LightGray.copy(alpha = 0.4f))),
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (value.isNullOrBlank()) "Sense dades registrades" else value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        color = if (value.isNullOrBlank()) Color.LightGray else Color.Black.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}