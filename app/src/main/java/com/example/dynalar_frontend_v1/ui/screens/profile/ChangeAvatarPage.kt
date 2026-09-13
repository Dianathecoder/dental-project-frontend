package com.example.dynalar_frontend_v1.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.doctorImages
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import java.io.File
import java.io.FileOutputStream

// Función auxiliar para cargar las fotos que el usuario ha guardado localmente
fun loadCustomAvatars(context: Context, userId: Long): List<Uri> {
    val dir = context.filesDir
    val files = dir.listFiles { _, name -> name.startsWith("avatar_${userId}_") }
    // Ordenamos para que las fotos más recientes salgan primero
    files?.sortByDescending { it.lastModified() }
    return files?.map { Uri.fromFile(it) } ?: emptyList()
}

@Composable
fun ChangeAvatarPage(
    onAvatarSelected: (Any) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val userViewModel: com.example.dynalar_frontend_v1.viewmodel.UserViewModel = viewModel()

    val savedUri = prefs.getString("user_avatar_uri_$userId", null)
    val savedResId = prefs.getInt("user_avatar_$userId", doctorImages.first())

    var selectedAvatarRes by remember { mutableStateOf<Int?>(if (savedUri == null) savedResId else null) }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(savedUri?.let { Uri.parse(it) }) }

    // Estado que guarda la lista de fotos personalizadas
    var customAvatars by remember { mutableStateOf(loadCustomAvatars(context, userId)) }

    // Lógica para borrar un avatar personalizado
    fun deleteCustomAvatar(uri: Uri) {
        uri.path?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        // Recargamos la lista
        customAvatars = loadCustomAvatars(context, userId)

        // Si borramos el que teníamos seleccionado, volvemos a uno por defecto
        if (selectedAvatarUri == uri) {
            selectedAvatarUri = null
            selectedAvatarRes = doctorImages.first()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                selectedAvatarUri = Uri.fromFile(file)
                selectedAvatarRes = null
                customAvatars = loadCustomAvatars(context, userId) // Recargamos la galería
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            try {
                val file = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                selectedAvatarUri = Uri.fromFile(file)
                selectedAvatarRes = null
                customAvatars = loadCustomAvatars(context, userId) // Recargamos la galería
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            Box(modifier = Modifier.padding(top = 32.dp)) {
                CustomTopBar(
                    title = stringResource(R.string.change_avatar_title),
                    onNavigateBack = onBack
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val editor = prefs.edit()

                        if (selectedAvatarUri != null) {
                            val uriString = selectedAvatarUri.toString()
                            editor.putString("user_avatar_uri_$userId", uriString)
                            editor.remove("user_avatar_$userId")
                            editor.apply()

                            userViewModel.updateUserAvatar(uriString) {
                                onAvatarSelected(uriString)
                            }

                        } else if (selectedAvatarRes != null) {
                            editor.putInt("user_avatar_$userId", selectedAvatarRes!!)
                            editor.remove("user_avatar_uri_$userId")
                            editor.apply()

                            userViewModel.updateUserAvatar("") {
                                onAvatarSelected(selectedAvatarRes!!)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
                ) {
                    Text(
                        text = stringResource(R.string.btn_save_changes),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.change_avatar_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C3E50)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = ButtonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeria")
                }
                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = ButtonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Càmera")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // El avatar grande destacado ahora siempre muestra lo que esté seleccionado
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (selectedAvatarUri != null) {
                    AsyncImage(
                        model = selectedAvatarUri,
                        contentDescription = "Avatar Seleccionat",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ButtonPrimary, CircleShape)
                    )
                } else if (selectedAvatarRes != null) {
                    Image(
                        painter = painterResource(id = selectedAvatarRes!!),
                        contentDescription = "Avatar Seleccionat",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ButtonPrimary, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // SECCIÓN 1: FOTOS PERSONALIZADAS
                if (customAvatars.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Text("Les meves fotos", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    items(customAvatars) { uri ->
                        val isSelected = selectedAvatarUri == uri

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                        ) {
                            // La imagen en sí
                            AsyncImage(
                                model = uri,
                                contentDescription = "La meva foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(if (isSelected) ButtonPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) ButtonPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarUri = uri
                                        selectedAvatarRes = null
                                    }
                            )

                            // Botoncito de Eliminar (X) en la esquina superior derecha
                            IconButton(
                                onClick = { deleteCustomAvatar(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color(0xFFE53935), CircleShape) // Rojo
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Esborrar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // SECCIÓN 2: AVATARES POR DEFECTO
                item(span = { GridItemSpan(3) }) {
                    Text("Avatars per defecte", color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                items(doctorImages) { imageRes ->
                    val isSelected = selectedAvatarRes == imageRes

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) ButtonPrimary.copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) ButtonPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedAvatarRes = imageRes
                                selectedAvatarUri = null
                            }
                            .padding(if (isSelected) 4.dp else 0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}