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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import coil.compose.AsyncImage
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.ui.components.CustomTopBar
import com.example.dynalar_frontend_v1.ui.components.doctorImages
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.utils.SessionManager
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChangeAvatarPage(
    onAvatarSelected: (Any) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val savedUri = prefs.getString("user_avatar_uri_$userId", null)
    val savedResId = prefs.getInt("user_avatar_$userId", doctorImages.first())

    var selectedAvatarRes by remember { mutableStateOf<Int?>(if (savedUri == null) savedResId else null) }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(savedUri?.let { Uri.parse(it) }) }

    // MAGIA AÑADIDA AQUÍ: Copiar la imagen de la galería a la app permanentemente
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                // Le ponemos el timestamp al nombre para que Coil no use caché vieja
                val file = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                selectedAvatarUri = Uri.fromFile(file)
                selectedAvatarRes = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // MAGIA AÑADIDA AQUÍ: Guardar la foto de la cámara permanentemente
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
                            editor.putString("user_avatar_uri_$userId", selectedAvatarUri.toString())
                            editor.remove("user_avatar_$userId")
                            editor.apply()
                            onAvatarSelected(selectedAvatarUri.toString())
                        } else if (selectedAvatarRes != null) {
                            editor.putInt("user_avatar_$userId", selectedAvatarRes!!)
                            editor.remove("user_avatar_uri_$userId")
                            editor.apply()
                            onAvatarSelected(selectedAvatarRes!!)
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

            if (selectedAvatarUri != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = selectedAvatarUri,
                        contentDescription = "Avatar Personalizado",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ButtonPrimary, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text("Avatars per defecte", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
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