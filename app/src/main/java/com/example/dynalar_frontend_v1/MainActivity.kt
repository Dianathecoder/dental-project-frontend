package com.example.dynalar_frontend_v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynalar_frontend_v1.ui.AppRoutes
import com.example.dynalar_frontend_v1.ui.screens.BoxPage
import com.example.dynalar_frontend_v1.ui.screens.CalendarPage
import com.example.dynalar_frontend_v1.ui.screens.CreateProfilePage
import com.example.dynalar_frontend_v1.ui.screens.DateInformationPage
import com.example.dynalar_frontend_v1.ui.screens.EditPatientPage
import com.example.dynalar_frontend_v1.ui.screens.HomePage
import com.example.dynalar_frontend_v1.ui.screens.ListPatientsScreen
import com.example.dynalar_frontend_v1.ui.screens.ListProtocolsPage
import com.example.dynalar_frontend_v1.ui.screens.ListStockPage
import com.example.dynalar_frontend_v1.ui.screens.LoginPage
import com.example.dynalar_frontend_v1.ui.screens.MaterialsHome
import com.example.dynalar_frontend_v1.ui.screens.OdontogramPage
import com.example.dynalar_frontend_v1.ui.screens.PatientProfilePage
import com.example.dynalar_frontend_v1.ui.screens.ProtocolPage
import com.example.dynalar_frontend_v1.ui.screens.PatientFileUploadPage
import com.example.dynalar_frontend_v1.ui.screens.PatientFilesPage
import com.example.dynalar_frontend_v1.ui.screens.ResumeDateScreen
import com.example.dynalar_frontend_v1.ui.screens.ScheduleAppointmentPage
import com.example.dynalar_frontend_v1.ui.screens.StockPage
import com.example.dynalar_frontend_v1.ui.screens.ToothPage
import com.example.dynalar_frontend_v1.ui.screens.UserProfilePage
import com.example.dynalar_frontend_v1.ui.theme.Dynalar_frontend_v1Theme
import com.example.dynalar_frontend_v1.viewmodel.AppointmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.BoxViewModel
import com.example.dynalar_frontend_v1.viewmodel.MaterialViewModel
import com.example.dynalar_frontend_v1.viewmodel.OdontogramViewModel
import com.example.dynalar_frontend_v1.viewmodel.PatientViewModel
import com.example.dynalar_frontend_v1.viewmodel.TreatmentViewModel
import com.example.dynalar_frontend_v1.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Dynalar_frontend_v1Theme {

                val navController = rememberNavController()
                val patientViewModel: PatientViewModel = viewModel()
                val materialViewModel: MaterialViewModel = viewModel()
                val treatmentViewModel: TreatmentViewModel = viewModel()
                val appointmentViewModel: AppointmentViewModel = viewModel()
                val odontogramViewModel: OdontogramViewModel = viewModel()
                val boxViewModel: BoxViewModel = viewModel()
                val userViewModel: UserViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.Login.route
                ) {

                    composable(AppRoutes.Login.route) {
                        LoginPage(
                            viewModel = userViewModel,
                            onLoginSuccess = {
                                navController.navigate(AppRoutes.Home.route)
                            },
                        )
                    }

                    composable(AppRoutes.Home.route) {
                        HomePage(
                            onNavigateListPacient = {
                                navController.navigate(AppRoutes.ListPatients.route)
                            },
                            onNavigateProfileUserProfile = {
                                navController.navigate(AppRoutes.UserProfile.route)
                            },
                            onNavigateBoxCalendar = {
                                navController.navigate(AppRoutes.CalendarPage.route)
                            },
                            onNavigateToAppointmentDetail = { appointment ->
                                appointmentViewModel.selectedAppointment = appointment
                                navController.navigate(AppRoutes.ResumeDate.route)
                            },
                            onNavigateBoxMaterials = {
                                navController.navigate(AppRoutes.MaterialsHome.route)
                            },
                            // !!! AÑADE ESTA LÍNEA AQUÍ PARA CONECTAR LA NAVEGACIÓN !!!
                            onNavigateToPatientProfile = { patientId ->
                                navController.navigate(AppRoutes.PatientProfile.createRoute(patientId))
                            }
                        )
                    }

                    composable(AppRoutes.MaterialsHome.route) {
                        MaterialsHome(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateBox = {
                                navController.navigate(AppRoutes.BoxPage.route)
                            },
                            onNavigateStock = {
                                navController.navigate(AppRoutes.ListStock.route)                            },
                            onNavigateProtocolo = {
                                navController.navigate(AppRoutes.ListProtocols.route)                            }
                        )
                    }

                    composable(AppRoutes.ListPatients.route) {
                        ListPatientsScreen(
                            viewModel = patientViewModel,
                            onNavigateAddPatient = { navController.navigate(AppRoutes.CreateProfile.route) },
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPatientProfile = { patientId ->
                                navController.navigate(AppRoutes.PatientProfile.createRoute(patientId))
                            }
                        )
                    }

                    composable(
                        route = AppRoutes.PatientProfile.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.LongType })
                    ) { backStackEntry ->

                        val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L

                        LaunchedEffect(patientId) {
                            if (patientId != -1L) {
                                patientViewModel.getPatientById(patientId)
                            }
                        }

                        val patient = patientViewModel.selectedPatient

                        if (patient != null) {

                            PatientProfilePage(
                                patient = patient,
                                appointmentViewModel = appointmentViewModel,

                                onBackClick = {
                                    navController.popBackStack()
                                },

                                onDeleteClick = { id ->
                                    patientViewModel.deletePatient(id) {
                                        navController.navigate(AppRoutes.ListPatients.route) {
                                            popUpTo(AppRoutes.PatientProfile.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onOdontogramClick = {
                                    patient.odontogram?.id?.let { odontogramId ->
                                        navController.navigate(
                                            AppRoutes.OdontogramPage.createRoute(odontogramId)
                                        )
                                    }
                                },

                                onEditClick = { id ->
                                    navController.navigate(
                                        AppRoutes.EditPatient.createRoute(id)
                                    )
                                },

                                onAppointmentClick = { appointment ->
                                    appointmentViewModel.selectedAppointment = appointment
                                    navController.navigate(AppRoutes.ResumeDate.route)
                                },

                                onFilesClick = {
                                    patient.id?.let { id ->
                                        navController.navigate(
                                            AppRoutes.PatientFiles.createRoute(id)
                                        )
                                    }
                                },

                                onCalendarClick = {
                                    navController.navigate(AppRoutes.CalendarPage.route)
                                },

                                onDateInformationClick = {
                                    patient.id?.let { id ->
                                        navController.navigate(
                                            AppRoutes.DateInformationPage.createRoute(id)
                                        )
                                    }
                                }
                            )

                        } else {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    composable(
                        route = AppRoutes.EditPatient.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L

                        LaunchedEffect(patientId) {
                            if (patientViewModel.selectedPatient?.id != patientId) {
                                patientViewModel.getPatientById(patientId)
                            }
                        }

                        val patient = patientViewModel.selectedPatient
                        if (patient != null) {
                            EditPatientPage(
                                patient = patient,
                                patientViewModel = patientViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // PANTALLA DE STOCK
                    composable(
                        route = AppRoutes.ListStock.route
                    ){
                        ListStockPage(materialViewModel,
                            onBack ={ navController.popBackStack() },
                            onMaterialClick = {
                                    material ->
                                material.id?.let { id ->
                                    navController.navigate(AppRoutes.StockPage.createRoute(id))
                                }
                            }
                        )
                    }

                    composable(
                        route = AppRoutes.StockPage.route,
                        arguments = listOf(navArgument("materialId") { type = NavType.LongType })
                    ) { backStackEntry ->

                        val materialId = backStackEntry.arguments?.getLong("materialId") ?: 0L

                        StockPage(
                            materialId = materialId,
                            viewModel = materialViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // PANTALLA DE PROTOCOLOS
                    composable(
                        route = AppRoutes.ListProtocols.route
                    ) {
                        ListProtocolsPage (
                            viewModel = treatmentViewModel,
                            onBack ={ navController.popBackStack() },
                            onTreatmentClick = {
                                treatment ->
                                treatment.id?.let { id ->
                                    navController.navigate(AppRoutes.ProtocolPage.createRoute(id))
                                }
                            }
                        )
                    }

                    // PANTALLA DE PROTOCOLO ESPECIFICO
                    composable(
                        route = AppRoutes.ProtocolPage.route,
                        arguments = listOf(navArgument("treatmentId") { type = NavType.LongType })
                    ) { backStackEntry ->

                        val treatmentId = backStackEntry.arguments?.getLong("treatmentId")
                            ?: return@composable

                        ProtocolPage(
                            treatmentId = treatmentId,
                            materialViewModel,
                            treatmentViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // PANTALLA DEL ODONTOGRAMA
                    composable(
                        route = AppRoutes.OdontogramPage.route,
                        arguments = listOf(
                            navArgument("odontogramId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        val odontogramId = backStackEntry.arguments?.getLong("odontogramId") ?: 0L

                        OdontogramPage(
                            odontogramId = odontogramId,
                            viewModel = odontogramViewModel,
                            onBack = { navController.popBackStack() },
                            onToothSelected = { toothNumber ->
                                navController.navigate(AppRoutes.ToothPage.createRoute(odontogramId, toothNumber))
                            }
                        )
                    }

                    composable(
                        route = AppRoutes.ToothPage.route,
                        arguments = listOf(
                            navArgument("odontogramId") { type = NavType.LongType },
                            navArgument("number") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val odontogramId = backStackEntry.arguments?.getLong("odontogramId") ?: 0L
                        val number = backStackEntry.arguments?.getInt("number") ?: 0

                        ToothPage(
                            number = number,
                            odontogramId = odontogramId,
                            viewModel = odontogramViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }


                    composable(AppRoutes.UserProfile.route) {
                        UserProfilePage(
                            viewModel = userViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoutes.CreateProfile.route) {
                        CreateProfilePage(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            patientViewModel = patientViewModel
                        )
                    }

                    composable(
                        route = AppRoutes.PatientFiles.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L

                        PatientFilesPage(
                            patientId = patientId,
                            patientViewModel = patientViewModel,
                            onBackClick = { navController.popBackStack() },
                            onNavigateUpload = {
                                navController.navigate(AppRoutes.PatientFileUpload.createRoute(patientId))
                            }
                        )
                    }

                    composable(AppRoutes.BoxPage.route){
                        BoxPage(onBack = {navController.popBackStack()}, viewModel = boxViewModel)
                    }

                    composable(
                        route = AppRoutes.PatientFileUpload.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L

                        PatientFileUploadPage(
                            patientId = patientId,
                            patientViewModel = patientViewModel,
                            onBackClick = { navController.popBackStack() },
                            onUploadDone = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(AppRoutes.CalendarPage.route) {
                        CalendarPage(
                            viewModel = appointmentViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onAddAppointmentClick = { date, hour, minute ->
                                navController.navigate(AppRoutes.ScheduleAppointment.createRoute(date.toString(), hour, minute))
                            },
                            onAppointmentClick = { appointment ->
                                appointmentViewModel.selectedAppointment = appointment
                                navController.navigate(AppRoutes.ResumeDate.route)
                            }
                        )
                    }

                    composable(AppRoutes.ResumeDate.route) {
                        val appointment = appointmentViewModel.selectedAppointment
                        if (appointment != null) {
                            ResumeDateScreen(
                                appointment = appointment,
                                onBackClick = { navController.popBackStack() },
                                onPatientClick = { patientId ->
                                    navController.navigate(AppRoutes.PatientProfile.createRoute(patientId))
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }


                    composable(
                        route = AppRoutes.ScheduleAppointment.route,
                        arguments = listOf(
                            navArgument("date") { type = NavType.StringType },
                            navArgument("hour") { type = NavType.IntType },
                            navArgument("minute") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                        val hour = backStackEntry.arguments?.getInt("hour") ?: 9
                        val minute = backStackEntry.arguments?.getInt("minute") ?: 0

                        ScheduleAppointmentPage(
                            initialDate = java.time.LocalDate.parse(dateStr),
                            initialHour = hour,
                            initialMinute = minute,
                            patientViewModel = patientViewModel,
                            appointmentViewModel = appointmentViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = AppRoutes.DateInformationPage.route,
                        arguments = listOf(navArgument("patientId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L

                        LaunchedEffect(patientId) {
                            if (patientId != -1L) {
                                patientViewModel.getPatientById(patientId)
                            }
                        }

                        val patient = patientViewModel.selectedPatient

                        if (patient != null) {
                            DateInformationPage(
                                patient = patient,
                                onBackClick = { navController.popBackStack() },
                                onUpdatePatient = { updatedPatient ->
                                    patientViewModel.updatePatient(updatedPatient)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}