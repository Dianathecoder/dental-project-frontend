package com.example.dynalar_frontend_v1.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynalar_frontend_v1.model.patient.Document
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.DeleteConfirmationDialog
import com.example.dynalar_frontend_v1.ui.components.Navegate_Button
import com.example.dynalar_frontend_v1.ui.components.SwipeToDeleteContainer
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun PatientFilesPage(
    patientId: Long,
    patientViewModel: PatientViewModel,
    onBackClick: () -> Unit,
    onNavigateUpload: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<Document?>(null) }

    LaunchedEffect(patientId) {
        if (patientId != -1L) {
            patientViewModel.getPatientById(patientId)
        }
    }

    val files = patientViewModel.selectedPatient?.documents.orEmpty()

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Arxius del pacient",
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
            Spacer(modifier = Modifier.height(8.dp))

            Navegate_Button(
                text = "Pujar arxiu",
                onClick = onNavigateUpload,
                modifier = Modifier.align(Alignment.End),
                icon = Icons.Default.UploadFile,
                iconSize = 18.dp,
                height = 40.dp,
                cornerRadius = 24.dp,
                fillMaxWidth = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (files.isEmpty()) {
                EmptyFilesState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files, key = { it.id ?: it.documentUrl.orEmpty() }) { file ->
                        SwipeToDeleteContainer(
                            onDelete = {
                                fileToDelete = file
                                showDeleteDialog = true
                            }
                        ) {
                            FileCard(
                                file = file,
                                onOpen = { openFileExternally(context, file) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && fileToDelete != null) {
        DeleteConfirmationDialog(
            message = "Estàs segur que vols eliminar aquest arxiu? Aquesta acció no es pot desfer.",
            confirmText = "Sí, eliminar",
            cancelText = "Cancel·lar",
            onConfirm = {
                val documentId = fileToDelete?.id
                if (documentId != null) {
                    patientViewModel.deletePatientDocument(patientId, documentId)
                }
                showDeleteDialog = false
                fileToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                fileToDelete = null
            }
        )
    }
}

@Composable
private fun EmptyFilesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = ButtonPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Encara no hi ha arxius",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Afegeix imatges o documents per veure'ls aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun FileCard(
    file: Document,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val type = file.type.orEmpty().lowercase()
            val url = file.documentUrl.orEmpty()
            if (type.startsWith("image/") || isImageByExtension(url)) {
                RemoteImagePreview(
                    documentId = file.id,
                    contentDescription = file.documentUrl
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF3F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = ButtonPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.documentUrl?.substringAfterLast('/')?.ifBlank { "Arxiu" } ?: "Arxiu",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = file.type ?: "Tipus desconegut",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                file.creationDate?.let { creationDate ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = creationDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF7A7A7A)
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteImagePreview(
    documentId: Long?,
    contentDescription: String?
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, documentId) {
        value = documentId?.let { id ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = URL("http://10.0.2.2:8080/document/$id").openStream().use { it.readBytes() }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }.getOrNull()
            }
        }
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEFF3F8)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = ButtonPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private fun isImageByExtension(url: String): Boolean {
    val lowercase = url.lowercase()
    return lowercase.endsWith(".jpg") ||
        lowercase.endsWith(".jpeg") ||
        lowercase.endsWith(".png") ||
        lowercase.endsWith(".webp")
}

private fun openFileExternally(context: Context, file: Document) {
    val fileId = file.id ?: return
    val mimeType = resolveMimeType(file)
    val uri = "http://10.0.2.2:8080/document/$fileId".toUri()

    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        val chooser = Intent.createChooser(viewIntent, "Obrir arxiu amb")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No s'ha trobat cap app per obrir aquest arxiu",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun resolveMimeType(file: Document): String {
    val url = file.documentUrl.orEmpty().lowercase()
    return when {
        url.endsWith(".jpg") || url.endsWith(".jpeg") -> "image/jpeg"
        url.endsWith(".png") -> "image/png"
        url.endsWith(".webp") -> "image/webp"
        url.endsWith(".pdf") -> "application/pdf"
        else -> "*/*"
    }
}

