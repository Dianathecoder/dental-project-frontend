    package com.example.dynalar_frontend_v1.ui.screens

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel

@Composable
fun PatientFileUploadPage(
    patientId: Long,
    patientViewModel: PatientViewModel,
    onBackClick: () -> Unit,
    onUploadDone: () -> Unit
) {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val uploadState = patientViewModel.uploadState

    LaunchedEffect(uploadState) {
        if (uploadState is InterfaceGlobal.Error) {
            uploadError = uploadState.message ?: "No s'han pogut pujar els arxius"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = (selectedUris + uris).distinct()
        }
    }

    val documentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = (selectedUris + uris).distinct()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Pujar arxiu",
                onNavigateBack = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFEFF3F8), RoundedCornerShape(60.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = ButtonPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Navegate_Button(
                        text = "Pujar Arxiu",
                        onClick = { showSourceDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedUris.isNotEmpty()) {
                Text(
                    text = "Seleccionats (${selectedUris.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedUris, key = { it.toString() }) { uri ->
                        SelectedFileItem(uri = uri, contentResolver = context.contentResolver)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Navegate_Button(
                    text = "Acceptar i guardar",
                    onClick = {
                        uploadError = null
                        patientViewModel.uploadPatientFiles(
                            context = context,
                            patientId = patientId,
                            uris = selectedUris,
                            onSuccess = onUploadDone
                        )
                    },
                    isLoading = uploadState is InterfaceGlobal.Loading,
                    enabled = uploadState !is InterfaceGlobal.Loading
                )

                if (uploadError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uploadError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB00020)
                    )
                }
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Selecciona origen") },
            text = { Text("Pots pujar des de galeria o documents.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Galeria")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSourceDialog = false
                        documentsLauncher.launch(arrayOf("image/*", "application/pdf"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Documents")
                }
            }
        )
    }
}

@Composable
private fun SelectedFileItem(uri: Uri, contentResolver: ContentResolver) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                tint = ButtonPrimary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = getDisplayName(contentResolver, uri) ?: uri.lastPathSegment.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun getDisplayName(contentResolver: ContentResolver, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    }
}

