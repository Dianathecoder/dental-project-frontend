package com.example.dynalar_frontend_v1.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
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
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.BannerGenericProfile
import com.example.dynalar_frontend_v1.ui.components.ErrorScreenWithImage
import com.example.dynalar_frontend_v1.ui.components.InputField
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePage(
    viewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToChangeAvatar: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val profileState by viewModel.profileUiState.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Leer el avatar específico de este usuario
    val customAvatarUri = prefs.getString("user_avatar_uri_$userId", null)
    val defaultAvatarResId = prefs.getInt("user_avatar_$userId", R.drawable.avatar_color)

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold(containerColor = FondoPagina) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // LA CLAVE ESTÁ AQUÍ: Si falla el endpoint, carga la caché de SessionManager
            val userDataFromApi = (profileState as? InterfaceGlobal.Success)?.data
            val userData = userDataFromApi ?: User(
                id = userId,
                name = "Usuari",
                surname = "",
                email = "",
                roles = emptyList()
            )

            val userRoleText = when {
                sessionManager.hasRole("SUPERADMIN") || sessionManager.hasRole("ROLE_SUPERADMIN") -> stringResource(id = R.string.role_superadmin)
                sessionManager.hasRole("OWNER") || sessionManager.hasRole("ROLE_OWNER") -> stringResource(id = R.string.role_owner)
                sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN") -> stringResource(id = R.string.role_admin)
                sessionManager.hasRole("DENTIST") || sessionManager.hasRole("DOCTOR") || sessionManager.hasRole("ROLE_DOCTOR") -> stringResource(id = R.string.role_doctor)
                sessionManager.hasRole("AUXILIAR") || sessionManager.hasRole("ROLE_AUXILIAR") -> stringResource(id = R.string.role_auxiliar)
                sessionManager.hasRole("PATIENT") || sessionManager.hasRole("ROLE_PATIENT") -> stringResource(id = R.string.role_patient)
                else -> stringResource(id = R.string.role_admin)
            }

            BannerGenericProfile(
                userName = userData?.name ?: "Usuari",
                userRole = userRoleText,
                profileImage = {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { onNavigateToChangeAvatar() }
                    ) {
                        // Mostrar imagen de Galería (Coil) o el Avatar Vectorial
                        if (customAvatarUri != null) {
                            AsyncImage(
                                model = customAvatarUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = defaultAvatarResId),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.33f)
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Canviar avatar", tint = Color.White, modifier = Modifier.size(16.dp).padding(bottom = 2.dp))
                        }
                    }
                },
                onNavigateBack = onNavigateBack,
                content = {}
            )

            // Si tenemos userData, ignoramos el error de red y pintamos los datos
            if (userData != null) {
                Box(modifier = Modifier.weight(1f)) {
                    UserInfoContent(
                        userData = userData,
                        onLogout = {
                            sessionManager.clearSession()
                            context.getSharedPreferences("dynalar_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                            onLogout()
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ButtonPrimary)
                }
            }
        }
    }
}

@Composable
fun UserInfoContent(userData: User, onLogout: () -> Unit = {}) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        InputField(label = stringResource(id = R.string.user_name_label), value = userData.name ?: "")
        InputField(label = stringResource(id = R.string.user_surname_label), value = userData.surname ?: "")
        InputField(label = stringResource(id = R.string.user_email_label), value = userData.email ?: "")
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Tancar Sessió", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}