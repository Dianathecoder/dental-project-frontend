package com.example.dynalar_frontend_v1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.CustomisableButtonMaterials


@Composable
fun MaterialsHome(
    onNavigateBack: () -> Unit,
    onNavigateBox: () -> Unit,
    onNavigateStock: () -> Unit,
    onNavigateProtocolo: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {

        Spacer(modifier = Modifier.height(27.dp))


        CustomTopBar(
            title = "Gestió",
            onNavigateBack = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Buttons_MaterialsPage(
                onNavigateBox = onNavigateBox,
                onNavigateStock = onNavigateStock,
                onNavigateProtocolo = onNavigateProtocolo
            )
        }
    }
}

@Composable
fun Buttons_MaterialsPage(
    modifier: Modifier = Modifier,
    onNavigateBox: () -> Unit,
    onNavigateStock: () -> Unit,
    onNavigateProtocolo: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        CustomisableButtonMaterials(
            iconRes = R.drawable.clinica_dental,
            title = "Box",
            subtitle = "Gestió de Boxes",
            onClick = onNavigateBox
        )

        Spacer(modifier = Modifier.height(60.dp))

        CustomisableButtonMaterials(
            iconRes = R.drawable.stock,
            title = "Stock Materials",
            subtitle = "Gestión de inventario stock",
            onClick = onNavigateStock
        )

        Spacer(modifier = Modifier.height(60.dp))

        CustomisableButtonMaterials(
            iconRes = R.drawable.protocolo,
            title = "Protocol",
            subtitle = "Materiales de dentista",
            onClick = onNavigateProtocolo
        )
    }
}