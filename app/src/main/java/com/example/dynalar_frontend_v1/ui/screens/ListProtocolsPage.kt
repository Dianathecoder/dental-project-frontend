package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.Treatment
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel

@Composable
fun ListProtocolsPage(
    viewModel: TreatmentViewModel = viewModel(),
    onTreatmentClick: (Treatment) -> Unit,
    onBack: () -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.getTreatments()
    }

    val uiState = viewModel.uiStateTreatment

    Scaffold(
        containerColor = Color(0xFFF8F9FB) // Mismo fondo que en Materiales y Boxes
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            CustomTopBar(
                title = "Protocols",
                onNavigateBack = onBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is InterfaceGlobal.Idle -> { }
                    is InterfaceGlobal.Loading -> {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                    is InterfaceGlobal.Success -> {
                        val validProtocols = uiState.data.filter { !it.name.isNullOrBlank() }

                        if (validProtocols.isEmpty()) {
                            // Mostrar icono si la lista está vacía
                            EmptyProtocolsState(modifier = Modifier.fillMaxSize())
                        } else {
                            val groupedProtocols = validProtocols.groupBy { it.name!!.first().uppercaseChar() }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                groupedProtocols.forEach { (initial, protocolList) ->
                                    item { CharacterHeaderProtocol(initial) }

                                    items(protocolList, key = { it.id ?: 0L }) { treatment ->
                                        TreatmentItem(
                                            treatment = treatment,
                                            onClick = { onTreatmentClick(treatment) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is InterfaceGlobal.NotFound -> {
                        // Mostrar icono si el estado es NotFound
                        EmptyProtocolsState(modifier = Modifier.fillMaxSize())
                    }
                    is InterfaceGlobal.Error -> {
                        Text(
                            text = stringResource(id = R.string.error_msg_format, uiState.message ?: ""),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// Nuevo componente para el estado vacío (Igual que en Stock y Box)
@Composable
private fun EmptyProtocolsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFA0B2C0) // Color gris azulado
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.protocols_not_found), // Usamos tu recurso de string
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
fun CharacterHeaderProtocol(initial: Char) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent // Transparente para que se vea el fondo gris del Scaffold
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
fun TreatmentItem(treatment: Treatment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(12.dp) // Esquinas un poco más suaves
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = ButtonPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = treatment.name ?: stringResource(id = R.string.protocol_unspecified_name),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
            }
        }
    }
}