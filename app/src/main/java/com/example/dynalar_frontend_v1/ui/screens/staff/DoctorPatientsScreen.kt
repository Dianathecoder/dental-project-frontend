package com.example.dynalar_frontend_v1.ui.screens.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.filter.ClinicalFilter
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.PatientFilterDropdown
import com.example.dynalar_frontend_v1.ui.components.getPatientImage
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientsScreen(
    doctorId: Long,
    patientViewModel: PatientViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPatientProfile: (Long) -> Unit
) {
    val textFieldState = rememberTextFieldState()
    val listState = rememberLazyListState()
    var selectedClinicalFilter by remember { mutableStateOf(ClinicalFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        patientViewModel.getDoctorPatients(doctorId)
    }

    val doctorPatientsList: List<Patient> = patientViewModel.doctorPatients

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            CustomTopBar(
                title = "Pacients del Doctor",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchDoctorPatientBar(textFieldState = textFieldState)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                PatientFilterDropdown(
                    selectedClinicalFilter = selectedClinicalFilter,
                    sortAscending = sortAscending,
                    onClinicalFilterChanged = { selectedClinicalFilter = it },
                    onSortChanged = { sortAscending = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val query = textFieldState.text.toString().trim()

            val filteredPatients = remember(doctorPatientsList, query, selectedClinicalFilter, sortAscending) {
                doctorPatientsList
                    .filter { patient -> !patient.name.isNullOrBlank() }
                    .filter { patient ->
                        val fullName = "${patient.name ?: ""} ${patient.lastName ?: ""}".trim()
                        val dniVal = patient.dni ?: ""
                        if (query.isBlank()) true
                        else fullName.contains(query, ignoreCase = true) || dniVal.contains(query, ignoreCase = true)
                    }
                    .filter { patient ->
                        val hasInfections = !patient.medicalRecord?.infectiousDeceases.isNullOrBlank()
                        val hasAllergies = !patient.medicalRecord?.allergies.isNullOrBlank()

                        when (selectedClinicalFilter) {
                            ClinicalFilter.ALL -> true
                            ClinicalFilter.ALLERGIES -> hasAllergies
                            ClinicalFilter.INFECTIONS -> hasInfections
                            ClinicalFilter.HEALTHY -> !hasAllergies && !hasInfections
                        }
                    }
                    .let { list ->
                        if (sortAscending) list.sortedBy { (it.name ?: "").uppercase() }
                        else list.sortedByDescending { (it.name ?: "").uppercase() }
                    }
            }

            if (filteredPatients.isEmpty()) {
                EmptyDoctorPatientsState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                val groupedPatients = remember(filteredPatients) {
                    filteredPatients.groupBy {
                        (it.name ?: it.lastName ?: "?").first().uppercaseChar()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedPatients.forEach { (initial, patientGroup) ->
                        item { CharacterHeaderDoctor(initial) }

                        items(patientGroup, key = { it.id ?: 0L }) { patient ->
                            DoctorPatientItem(
                                patient = patient,
                                onClick = { selectedPatient ->
                                    selectedPatient.id?.let {
                                        onNavigateToPatientProfile(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchDoctorPatientBar(
    textFieldState: TextFieldState
) {
    val query = textFieldState.text.toString()

    OutlinedTextField(
        value = query,
        onValueChange = { text ->
            textFieldState.edit { replace(0, length, text) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
        placeholder = {
            Text(
                stringResource(R.string.patients_search_placeholder),
                color = Color.Gray.copy(alpha = 0.8f)
            )
        },
        shape = RoundedCornerShape(30.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedBorderColor = Color(0xFFA0B2C0),
            focusedBorderColor = ButtonPrimary,
            cursorColor = ButtonPrimary
        ),
        singleLine = true
    )
}

@Composable
private fun CharacterHeaderDoctor(initial: Char) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF4F6F9),
        tonalElevation = 1.dp
    ) {
        Text(
            text = initial.toString(),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ButtonPrimary
        )
    }
}

@Composable
private fun EmptyDoctorPatientsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aquest doctor no té pacients assignats.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
private fun DoctorPatientItem(
    patient: Patient,
    onClick: (Patient) -> Unit
) {
    val allergies = patient.medicalRecord?.allergies
    val infectiousDeceases = patient.medicalRecord?.infectiousDeceases

    val hasInfections = !infectiousDeceases.isNullOrBlank()
    val hasAllergies = !allergies.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick(patient) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getPatientImage(patient.id, patient.sex)),
                contentDescription = "Pacient",
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
                    text = "${patient.name ?: ""} ${patient.lastName ?: ""}".trim(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (hasInfections) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Coronavirus,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = infectiousDeceases!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (hasAllergies) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.patient_allergy_prefix, allergies),
                            color = Color(0xFFE65100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!hasInfections && !hasAllergies) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.patients_no_alerts),
                            color = Color(0xFF388E3C),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}