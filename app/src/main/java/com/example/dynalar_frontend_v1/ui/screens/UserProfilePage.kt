package com.example.dynalar_frontend_v1.ui.screens

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynalar_frontend_v1.R
import com.example.dynalar_frontend_v1.model.LoginUiState
import com.example.dynalar_frontend_v1.model.auth.AuthResponse
import com.example.dynalar_frontend_v1.model.user.User
import com.example.dynalar_frontend_v1.ui.components.BannerGenericProfile
import com.example.dynalar_frontend_v1.ui.components.ErrorScreenWithImage
import com.example.dynalar_frontend_v1.ui.components.InputField
import com.example.dynalar_frontend_v1.ui.theme.ButtonPrimary
import com.example.dynalar_frontend_v1.ui.theme.Dynalar_frontend_v1Theme
import com.example.dynalar_frontend_v1.ui.theme.FondoPagina
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalConfiguration
import com.example.dynalar_frontend_v1.utils.changeLanguage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePage(
    viewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.userUiState.collectAsState()

    Scaffold(
        containerColor = FondoPagina
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val authData = (uiState as? LoginUiState.Success)?.authResponse
            BannerGenericProfile(
                userName = authData?.name ?: "",
                userRole = if (authData?.role?.toString()?.uppercase() == "ADMIN") {
                    stringResource(id = R.string.role_admin)
                } else {
                    stringResource(id = R.string.role_user)
                },
                profileImage = {
                    Image(
                        painter = painterResource(R.drawable.avatar_color),
                        contentDescription = stringResource(id = R.string.profile_picture_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                },
                onNavigateBack = onNavigateBack,
                content = {}
            )

            when (uiState) {
                is LoginUiState.Loading, LoginUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonPrimary)
                    }
                }

                is LoginUiState.Success -> {
                    Box(modifier = Modifier.weight(1f)) {

                        UserInfoContent(authData = authData!!)
                    }
                }

                is LoginUiState.Error -> {
                    val message = (uiState as LoginUiState.Error).message
                    ErrorScreenWithImage(
                        message = message,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

//CÓDIGO NUEVO CORRECTO
@Composable
fun UserInfoContent(authData: AuthResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        InputField(label = stringResource(id = R.string.user_name_label), value = authData.name ?: "")
        InputField(label = stringResource(id = R.string.user_surname_label), value = authData.surname ?: "")
        InputField(label = stringResource(id = R.string.user_email_label), value = authData.email ?: "")

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