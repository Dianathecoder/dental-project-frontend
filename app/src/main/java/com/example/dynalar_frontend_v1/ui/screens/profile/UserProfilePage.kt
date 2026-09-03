package com.example.dynalar_frontend_v1.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.interfaces.InterfaceGlobal
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.BannerGenericProfile
import com.example.dynalar_frontend_v1.ui.components.ErrorScreenWithImage
import com.example.dynalar_frontend_v1.ui.components.InputField
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.Dynalar_frontend_v1Theme
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.utils.SessionManager
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePage(
    viewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToChangeAvatar: () -> Unit = {}
) {
    val profileState by viewModel.profileUiState.collectAsState()

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val currentAvatarResId = prefs.getInt("user_avatar", R.drawable.avatar_color)
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold(
        containerColor = FondoPagina
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val userData = (profileState as? InterfaceGlobal.Success)?.data

            // CORREGIDO: Usamos SessionManager para determinar el rol sin depender del campo interno de User
            val userRoleText = when {
                sessionManager.hasRole("ADMIN") || sessionManager.hasRole("ROLE_ADMIN") -> stringResource(id = R.string.role_admin)
                sessionManager.hasRole("DENTIST") || sessionManager.hasRole("DOCTOR") || sessionManager.hasRole("ROLE_DOCTOR") -> "Doctor/a"
                sessionManager.hasRole("AUXILIAR") || sessionManager.hasRole("ROLE_AUXILIAR") -> "Auxiliar"
                sessionManager.hasRole("PATIENT") || sessionManager.hasRole("ROLE_PATIENT") -> "Paciente"
                else -> stringResource(id = R.string.role_admin)
            }

            BannerGenericProfile(
                userName = userData?.name ?: "",
                userRole = userRoleText,
                profileImage = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { onNavigateToChangeAvatar() }
                    ) {
                        Image(
                            painter = painterResource(id = currentAvatarResId),
                            contentDescription = stringResource(id = R.string.profile_picture_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.33f)
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Canviar avatar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp).padding(bottom = 2.dp)
                            )
                        }
                    }
                },
                onNavigateBack = onNavigateBack,
                content = {}
            )

            when (profileState) {
                is InterfaceGlobal.Loading, InterfaceGlobal.Idle -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }

                is InterfaceGlobal.Success -> {
                    Box(modifier = Modifier.weight(1f)) {
                        UserInfoContent(userData = userData!!)
                    }
                }

                is InterfaceGlobal.Error -> {
                    val errorState = profileState as InterfaceGlobal.Error
                    val message = if (errorState.stringResId != null) {
                        stringResource(id = errorState.stringResId)
                    } else {
                        errorState.message ?: stringResource(id = R.string.error_generic)
                    }

                    ErrorScreenWithImage(
                        message = message,
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun UserInfoContent(userData: User) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        InputField(label = stringResource(id = R.string.user_name_label), value = userData.name ?: "")
        InputField(label = stringResource(id = R.string.user_surname_label), value = userData.surname ?: "")
        InputField(label = stringResource(id = R.string.user_email_label), value = userData.email ?: "")

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F4)
@Composable
fun UserProfilePagePreview() {
    Dynalar_frontend_v1Theme {
        UserProfilePage()
    }
}