package com.example.dynalar_frontend_v1.ui.screens


import PatientFilterDropdown
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.patient.Patient
import com.example.dynalar_frontend_v1.ui.components.AddButton
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.ErrorScreenWithImage
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.components.getPatientImage
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPatientsScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateAddPatient: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPatientProfile: (Long) -> Unit
) {
    val uiState = viewModel.uiStatePatient
    val textFieldState = rememberTextFieldState()
    val listState = rememberLazyListState()
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var sortAscending by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var patientToDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            PatientsTopBar(
                onNavigateAddPatient = onNavigateAddPatient,
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchPatientBar(textFieldState = textFieldState, viewModel = viewModel)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                PatientFilterDropdown(
                    selectedLetter = selectedLetter,
                    sortAscending = sortAscending,
                    onLetterSelected = { selectedLetter = it },
                    onSortChanged = { sortAscending = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is InterfaceGlobal.Loading, InterfaceGlobal.Idle -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }

                is InterfaceGlobal.Success -> {
                    val filteredPatients = remember(uiState.data, selectedLetter, sortAscending) {
                        uiState.data
                            .filter { patient -> !patient.name.isNullOrBlank() || !patient.lastName.isNullOrBlank() }
                            .filter { patient ->
                                selectedLetter == null ||
                                        (patient.name ?: patient.lastName ?: "").firstOrNull()?.uppercaseChar() == selectedLetter
                            }
                            .let { list ->
                                if (sortAscending) {
                                    list.sortedBy { (it.name ?: it.lastName ?: "").uppercase() }
                                } else {
                                    list.sortedByDescending { (it.name ?: it.lastName ?: "").uppercase() }
                                }
                            }
                    }
                    // Si la lista está vacía, mostramos el nuevo Empty State
                    if (filteredPatients.isEmpty()) {
                        EmptyPatientsState(modifier = Modifier.weight(1f).fillMaxWidth())
                    } else {
                        val patients = remember(filteredPatients) {
                            filteredPatients.groupBy {
                                (it.name ?: it.lastName ?: "?").first().uppercaseChar()
                            }
                        }

                        val lastPatientId = remember(filteredPatients) {
                            filteredPatients.lastOrNull()?.id
                        }

                        val firstPatientId = remember(filteredPatients) {
                            filteredPatients.firstOrNull()?.id
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            patients.forEach { (initial, patientList) ->
                                item { CharacterHeader(initial) }


                                items(patientList, key = { it.id ?: 0L }) { patient ->

                                    if (patient.id == lastPatientId) {
                                        LaunchedEffect(patient.id) {
                                            android.util.Log.d(
                                                "Pagination",
                                                ">>> S'ha arribat al final de la llista (ID: ${patient.id}). Disparant carga..."
                                            )
                                            viewModel.loadNextPage()
                                        }
                                    }

                                    val isFirstElement = (patient.id == firstPatientId)
                                    SwipeToDeleteContainer(
                                        enableHintAnimation = isFirstElement,
                                        hintAlreadyShown = viewModel.isDeleteHintShown,
                                        onHintShown = { viewModel.isDeleteHintShown = true },
                                        onDelete = {
                                            patientToDelete = patient.id
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        PatientItem(
                                            patient = patient,
                                            onClick = { selectedPatient ->
                                                selectedPatient.id?.let {
                                                    onNavigateToPatientProfile(
                                                        it
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            if (viewModel.isFetching) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = ButtonPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is InterfaceGlobal.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.message ?: stringResource(R.string.patients_empty_list),
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                }

                InterfaceGlobal.NotFound -> {
                    EmptyPatientsState(modifier = Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
    if (showDeleteDialog && patientToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                patientToDelete?.let { id ->
                    viewModel.deletePatient(id)
                }
                showDeleteDialog = false
                patientToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                patientToDelete = null
            }
        )
    }
}



@Composable
fun SearchPatientBar(
    textFieldState: TextFieldState,
    viewModel: PatientViewModel
) {
    val query = textFieldState.text.toString()

    LaunchedEffect(query) {
        kotlinx.coroutines.delay(300)
        if (query.isNotBlank()) {
            viewModel.searchPatients(query)
        } else {
            viewModel.getPatients()
        }
    }

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
fun AddPatientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AddButton(
        onClick = onClick,
        iconRes = R.drawable.person_add,
        modifier = modifier
    )
}

@Composable
fun PatientsTopBar(
    onNavigateBack: () -> Unit,
    onNavigateAddPatient: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        CustomTopBar(
            title = stringResource(R.string.patients_topbar_title),
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        AddPatientButton(
            onClick = onNavigateAddPatient,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 50.dp, top = 25.dp)
        )
    }
}

@Composable
fun CharacterHeader(initial: Char) {
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
private fun EmptyPatientsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group, // Icono de múltiples personas
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0) // Color gris azulado para seguir el estilo
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No registered patients.", // Puedes cambiarlo a stringResource(R.string...)
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
fun PatientItem(
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
                    text = "${patient.name ?: ""} ${patient.lastName ?: ""}",
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

                // --- 2. Alerta de Alergias ---
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