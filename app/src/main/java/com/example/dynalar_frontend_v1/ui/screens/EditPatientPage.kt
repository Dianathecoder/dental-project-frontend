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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.patient.MedicalRecord
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.model.patient.Sex
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.InputFieldEditable
import com.example.dynalar_frontend_v1.ui.components.PhoneInputField
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel

@Composable
fun EditPatientPage(
    patient: Patient,
    onNavigateBack: () -> Unit,
    patientViewModel: PatientViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }

    // --- INFORMACIÓN PERSONAL ---
    var name by remember { mutableStateOf(patient.name ?: "") }
    var lastName by remember { mutableStateOf(patient.lastName ?: "") }
    var email by remember { mutableStateOf(patient.email ?: "") }
    var dni by remember { mutableStateOf(patient.dni ?: "") }
    var sex by remember { mutableStateOf(patient.sex ?: Sex.MALE) }

    // --- TELÉFONO ---
    // Intentamos separar el código del país del número (Formato esperado: "+34 666777888")
    val phoneParts = (patient.phone ?: "+34 ").split(" ")
    var countryCode by remember { mutableStateOf(phoneParts.getOrNull(0) ?: "+34") }
    var phone by remember { mutableStateOf(phoneParts.getOrNull(1) ?: "") }

    // --- HISTORIAL CLÍNICO ---
    var familyHistory by remember { mutableStateOf(patient.medicalRecord?.familyHistory ?: "") }
    var dentalConditions by remember { mutableStateOf(patient.medicalRecord?.deceases ?: "") }
    var medicalNotes by remember { mutableStateOf(patient.medicalRecord?.medication ?: "") }
    var allergies by remember { mutableStateOf(patient.medicalRecord?.allergies ?: "") }
    var infectiousDeceases by remember { mutableStateOf(patient.medicalRecord?.infectiousDeceases ?: "") }

    val fillAllFieldsMsg = stringResource(R.string.validation_fill_all_fields)
    val invalidEmailMsg = stringResource(R.string.validation_invalid_email)
    val invalidPhoneMsg = stringResource(R.string.validation_invalid_phone)
    val invalidDniMsg = stringResource(R.string.validation_invalid_dni)
    val updateSuccessMsg = stringResource(R.string.patient_update_success)


    val context = LocalContext.current

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
                        text = "Guardar Canvis",
                        onClick = {
                            if (name.isBlank() || lastName.isBlank() || dni.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, fillAllFieldsMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }
                            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
                            if (!email.trim().matches(emailRegex)) {
                                Toast.makeText(context, invalidEmailMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            val phoneRegex = "^[0-9]{9}\$".toRegex()
                            if (!phone.trim().matches(phoneRegex)) {
                                Toast.makeText(context, invalidPhoneMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }

                            val dniRegex = "^[XYZxyz]?\\d{7,8}[A-Za-z]\$".toRegex()
                            if (!dni.trim().matches(dniRegex)) {
                                Toast.makeText(context, invalidDniMsg, Toast.LENGTH_SHORT).show()
                                return@Navegate_Button
                            }


                            val updatedPatient = patient.copy(
                                name = name,
                                lastName = lastName,
                                email = email,
                                dni = dni,
                                sex = sex,
                                phone = "$countryCode $phone",
                                medicalRecord = MedicalRecord(
                                    familyHistory = familyHistory,
                                    allergies = allergies,
                                    medication = medicalNotes,
                                    deceases = dentalConditions,
                                    infectiousDeceases = patient.medicalRecord?.infectiousDeceases ?: ""
                                )
                            )

                            patientViewModel.updatePatient(updatedPatient)
                            Toast.makeText(context, updateSuccessMsg, Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
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
            EditHeader_ButtonNavigator(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onNavigateBack = onNavigateBack,
            )

            Spacer(modifier = Modifier.height(35.dp))

            Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                if (selectedTab == 0) {

                    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                        InputFieldEditable(
                            label = stringResource(R.string.register_name),
                            value = name,
                            onValueChange = { name = it },
                            placeholder = stringResource(R.string.register_name)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.register_surname),
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = stringResource(R.string.register_surname)
                        )

                        Column {
                            Text(
                                text = stringResource(R.string.patient_sex),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Sex.values().forEach { option ->
                                    TabButton(
                                        text = when(option) {
                                            Sex.MALE -> stringResource(R.string.patient_sex_male)
                                            Sex.FEMALE -> stringResource(R.string.patient_sex_female)
                                            else -> ""
                                        },
                                        isSelected = sex == option,
                                        onClick = { sex = option },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        InputFieldEditable(
                            label = stringResource(R.string.login_email_label),
                            value = email,
                            onValueChange = { email = it },
                            placeholder = stringResource(R.string.login_email_placeholder)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.patient_dni),
                            value = dni,
                            onValueChange = { newValue ->
                                var filtered = newValue.uppercase().take(9)
                                filtered = filtered.filterIndexed { index, char ->
                                    if (index < 8) char.isDigit() else char.isLetter()
                                }
                                dni = filtered
                            },
                            placeholder = stringResource(R.string.patient_dni_placeholder)
                        )

                        PhoneInputField(
                            label = stringResource(R.string.patient_phone),
                            countryCode = countryCode,
                            onCountryCodeChange = { countryCode = it },
                            phoneNumber = phone,
                            onPhoneNumberChange = { newValue ->
                                phone = newValue.filter { it.isDigit() }.take(9)
                            }
                        )
                    }
                } else {

                    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                        InputFieldEditable(
                            label = stringResource(R.string.patient_family_history),
                            value = familyHistory,
                            onValueChange = { familyHistory = it },
                            placeholder = stringResource(R.string.placeholder_family_history)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.patient_dental_conditions),
                            value = dentalConditions,
                            onValueChange = { dentalConditions = it },
                            placeholder = stringResource(R.string.placeholder_dental_conditions)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.patient_medication),
                            value = medicalNotes,
                            onValueChange = { medicalNotes = it },
                            placeholder = stringResource(R.string.placeholder_medication)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.patient_allergies),
                            value = allergies,
                            onValueChange = { allergies = it },
                            placeholder = stringResource(R.string.placeholder_allergies)
                        )
                        InputFieldEditable(
                            label = stringResource(R.string.patient_infectious_diseases),
                            value = infectiousDeceases,
                            onValueChange = { infectiousDeceases = it },
                            placeholder = stringResource(R.string.placeholder_infectious_diseases)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditHeader_ButtonNavigator(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column {
        CustomTopBar(
            title = stringResource(R.string.patient_edit_title),
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
                text = stringResource(R.string.patient_tab_info),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = stringResource(R.string.patient_tab_history),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}