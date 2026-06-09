package com.example.dynalar_frontend_v1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.dynalar_frontend_v1.model.patient.MedicalRecord
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.model.patient.Sex
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.InputFieldEditable
import com.example.dynalar_frontend_v1.ui.components.PhoneInputField
import com.example.dynalar_frontend_v1.ui.components.SignaturePadControlled
import com.example.dynalar_frontend_v1.ui.components.ValidationAndSignatureDialog
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel

@Composable
fun CreateProfilePage(
    onNavigateBack: () -> Unit,
    patientViewModel: PatientViewModel = viewModel()
) {
    CreateProfileForm(
        onNavigateBack = onNavigateBack,
        patientViewModel = patientViewModel
    )
}

@Composable
fun CreateProfileForm(
    onNavigateBack: () -> Unit,
    patientViewModel: PatientViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Información Personal
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.MALE) }

    // Teléfono
    var countryCode by remember { mutableStateOf("+34") }
    var phone by remember { mutableStateOf("") }

    // Historial Clínico
    var familyHistory by remember { mutableStateOf("") }
    var dentalConditions by remember { mutableStateOf("") }
    var medicalNotes by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var infectiousDeceases by remember { mutableStateOf("") }

    var signatureStep by remember { mutableStateOf(0) }
    var tempAnesthesiaSignature by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Muestra Toast si createPatient falla, sin romper el estado de la lista
    LaunchedEffect(patientViewModel.crudError) {
        patientViewModel.crudError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            patientViewModel.clearCrudError()
        }
    }

    val performSave = { anesthesiaSig: String?, historySig: String? ->
        val newPatient = Patient(
            name = name,
            lastName = lastName,
            email = email,
            dni = dni,
            sex = sex,
            phone = "$countryCode $phone",
            anesthesiaConsent = !anesthesiaSig.isNullOrBlank(),
            medicalRecord = MedicalRecord(
                familyHistory = familyHistory,
                allergies = allergies,
                medication = medicalNotes,
                deceases = dentalConditions,
                infectiousDeceases = infectiousDeceases,
                signatureBase64 = anesthesiaSig,
                signatureConfirmation = historySig
            )
        )

        patientViewModel.createPatient(newPatient) {
            Toast.makeText(context, "Pacient creat correctament", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    if (signatureStep == 1) {
        ValidationAndSignatureDialog(
            title = "Consentiment Anestèsia",
            consentTitle = "Consentiment Anestèsia:",
            infectiousDeceases = infectiousDeceases,
            allergies = allergies,
            onConfirm = { signature ->
                tempAnesthesiaSignature = signature
                signatureStep = 2
            },
            onDismiss = { signatureStep = 0 }
        )
    } else if (signatureStep == 2) {
        ValidationAndSignatureDialog(
            title = "Confirmació d'Historial",
            consentTitle = "Confirmació de dades:",
            consentText = "El pacient confirma que les dades de l'historial mèdic, malalties i al·lèrgies revisades són correctes.",
            infectiousDeceases = infectiousDeceases,
            allergies = allergies,
            isOptional = false,
            onConfirm = { signature ->
                val historySignature = signature
                signatureStep = 0
                performSave(tempAnesthesiaSignature, historySignature)
            },
            onDismiss = { signatureStep = 0 }
        )
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Navegate_Button(
                        text = "Guarda i Continua",
                        onClick = {
                            if (name.isBlank() || lastName.isBlank() || email.isBlank() || dni.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, "Per favor, emplena tots els camps", Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
                            if (!email.matches(emailRegex)) {
                                Toast.makeText(context, "Correu electrònic invàlid", Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            val phoneRegex = "^[0-9]{9}$".toRegex()
                            if (!phone.trim().matches(phoneRegex)) {
                                Toast.makeText(context, "Format de telèfon invàlid (han de ser 9 dígits)", Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            val dniRegex = "^[XYZxyz]?\\d{7,8}[A-Za-z]$".toRegex()
                            if (!dni.trim().matches(dniRegex)) {
                                Toast.makeText(context, "Format de DNI o NIE invàlid", Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            signatureStep = 1
                        },
                        enabled = selectedTab == 1
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Header_ButtonNavigator(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onNavigateBack = onNavigateBack,
            )

            Spacer(modifier = Modifier.height(35.dp))

            Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                if (selectedTab == 0) {
                    InformationPersonal(
                        name = name, onNameChange = { name = it },
                        lastName = lastName, onLastNameChange = { lastName = it },
                        email = email, onEmailChange = { email = it },
                        dni = dni, onDniChange = { dni = it },
                        countryCode = countryCode, onCountryCodeChange = { countryCode = it },
                        phone = phone, onPhoneChange = { phone = it },
                        sex = sex, onSexChange = { sex = it },
                    )
                } else {
                    InformationMedical(
                        familyHistory = familyHistory, onFamilyHistoryChange = { familyHistory = it },
                        dentalConditions = dentalConditions, onDentalConditionsChange = { dentalConditions = it },
                        medicalNotes = medicalNotes, onMedicalNotesChange = { medicalNotes = it },
                        allergies = allergies, onAllergiesChange = { allergies = it },
                        infectiousDeceases = infectiousDeceases,
                        onInfectiousDeceasesChange = { infectiousDeceases = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun Header_ButtonNavigator(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column {
        CustomTopBar(
            title = "Nou Pacient",
            titleFontSize = 20.sp,
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(35.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Informació",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Historial Clínic",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(45.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF537895) else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color.DarkGray
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun InformationPersonal(
    name: String, onNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    dni: String, onDniChange: (String) -> Unit,
    countryCode: String, onCountryCodeChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    sex: Sex, onSexChange: (Sex) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
        InputFieldEditable(label = "Nom", value = name, onValueChange = onNameChange, placeholder = "Nom")
        InputFieldEditable(label = "Cognoms", value = lastName, onValueChange = onLastNameChange, placeholder = "Cognoms")

        Column {
            Text(
                text = "Sexe",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Sex.MALE, Sex.FEMALE).forEach { option ->
                    TabButton(
                        text = when(option) {
                            Sex.MALE -> "Home"
                            Sex.FEMALE -> "Dona"
                            else -> ""
                        },
                        isSelected = sex == option,
                        onClick = { onSexChange(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        InputFieldEditable(label = "Email", value = email, onValueChange = onEmailChange, placeholder = "correu@exemple.com")

        InputFieldEditable(
            label = "DNI",
            value = dni,
            onValueChange = { newValue ->
                var filtered = newValue.uppercase().take(9)
                filtered = filtered.filterIndexed { index, char ->
                    if (index < 8) char.isDigit() else char.isLetter()
                }
                onDniChange(filtered)
            },
            placeholder = "12345678X"
        )

        PhoneInputField(
            label = "Telèfon",
            countryCode = countryCode,
            onCountryCodeChange = onCountryCodeChange,
            phoneNumber = phone,
            onPhoneNumberChange = { newValue ->
                val filteredPhone = newValue.filter { it.isDigit() }.take(9)
                onPhoneChange(filteredPhone)
            }
        )
    }
}

@Composable
fun InformationMedical(
    familyHistory: String, onFamilyHistoryChange: (String) -> Unit,
    dentalConditions: String, onDentalConditionsChange: (String) -> Unit,
    medicalNotes: String, onMedicalNotesChange: (String) -> Unit,
    allergies: String, onAllergiesChange: (String) -> Unit,
    infectiousDeceases: String,
    onInfectiousDeceasesChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        InputFieldEditable(
            label = "Historial Familiar",
            value = familyHistory,
            onValueChange = onFamilyHistoryChange,
            placeholder = "Ej: Observacions..."
        )
        InputFieldEditable(
            label = "Condicions Dentals",
            value = dentalConditions,
            onValueChange = onDentalConditionsChange,
            placeholder = "Ej: Mal de dents"
        )
        InputFieldEditable(
            label = "Medicació",
            value = medicalNotes,
            onValueChange = onMedicalNotesChange,
            placeholder = "Paracetamol"
        )
        InputFieldEditable(
            label = "Al·lèrgies",
            value = allergies,
            onValueChange = onAllergiesChange,
            placeholder = "Ej: Penicilina"
        )
        InputFieldEditable(
            label = "Malalties Infeccioses",
            value = infectiousDeceases,
            onValueChange = onInfectiousDeceasesChange,
            placeholder = "Hepatitis, VIH, etc."
        )
    }
}