package com.example.dynalar_frontend_v1.ui

sealed class AppRoutes(val route: String) {
    object Login : AppRoutes("loginPage")
    object Home : AppRoutes("homePage")

    object ListPatients : AppRoutes("patientsScreen")
    object CreateProfile : AppRoutes("createProfile")
    object OdontogramPage : AppRoutes("odontogramPage/{odontogramId}") {
        fun createRoute(odontogramId: Long) = "odontogramPage/$odontogramId"
    }
    object ToothPage: AppRoutes("toothPage/{odontogramId}/{number}") {
        fun createRoute(odontogramId: Long, number: Int) = "toothPage/$odontogramId/$number"
    }

    object ListStock: AppRoutes("listStock")

    object StockPage: AppRoutes("stockPage/{materialId}") {
        fun createRoute(materialId: Long) = "stockPage/$materialId"
    }

    object ListProtocols: AppRoutes("listProtocols")

    object ProtocolPage: AppRoutes("protocolPage/{treatmentId}"){
        fun createRoute(treatmentId: Long) = "protocolPage/$treatmentId"
        }

    object UserProfile : AppRoutes("userProfile")
    object CalendarPage : AppRoutes("calendarPage")
    object PatientProfile : AppRoutes("patientProfile/{patientId}") {
        fun createRoute(patientId: Long) = "patientProfile/$patientId"
    }

    object EditPatient : AppRoutes("editPatient/{patientId}") {
        fun createRoute(patientId: Long) = "editPatient/$patientId"
    }

    object PatientFiles : AppRoutes("patientFiles/{patientId}") {
        fun createRoute(patientId: Long) = "patientFiles/$patientId"
    }

    object PatientFileUpload : AppRoutes("patientFileUpload/{patientId}") {
        fun createRoute(patientId: Long) = "patientFileUpload/$patientId"
    }

    object ResumeDate : AppRoutes("resumeDate")

    object DateInformationPage : AppRoutes("dateInformationPage/{patientId}") {
        fun createRoute(patientId: Long) = "dateInformationPage/$patientId"
    }

    object MaterialsHome : AppRoutes("materialsHome")

    object BoxPage : AppRoutes("boxPage")
    object ScheduleAppointment : AppRoutes("scheduleAppointment/{date}/{hour}/{minute}") {

        fun createRoute(date: String, hour: Int, minute: Int): String {
            return "scheduleAppointment/$date/$hour/$minute"
        }
    }
}