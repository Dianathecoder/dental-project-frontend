package com.example.dynalar_frontend_v1.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.CustomisableButtonMaterials

@Composable
fun ClinicalManagementHome(
    onNavigateBack: () -> Unit,
    onNavigateTreatments: () -> Unit,
    onNavigateDoctorAvailability: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Spacer(modifier = Modifier.height(27.dp))

        CustomTopBar(
            title = "Àrea Clínica",
            onNavigateBack = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CustomisableButtonMaterials(
                    iconRes = R.drawable.protocolo,
                    title = "Gestió de Tractaments",
                    subtitle = "Crear nous tractaments i configurar la durada",
                    onClick = onNavigateTreatments
                )

                Spacer(modifier = Modifier.height(30.dp))

                CustomisableButtonMaterials(
                    iconRes = R.drawable.clinica_dental,
                    title = "Horaris i Especialitats",
                    subtitle = "Assignar tractaments i torns als doctors",
                    onClick = onNavigateDoctorAvailability
                )
            }
        }
    }
}