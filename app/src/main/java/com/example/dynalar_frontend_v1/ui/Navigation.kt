package com.example.dynalar_frontend_v1.ui

sealed class AppRoutes(val route: String) {
    object Login : AppRoutes("loginPage")
    object Home : AppRoutes("homePage")

    object ListPatients : AppRoutes("patientsScreen")
    object CreatePatient : AppRoutes("createPatient")
    object CreateUser : AppRoutes("createUser")
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
    object ChangeAvatar : AppRoutes("changeAvatarPage")
    object Register : AppRoutes("registerPage")

    object AdminDashboard : AppRoutes("admin_dashboard")
    object AuxiliarDashboard : AppRoutes("auxiliar_dashboard")
    object DentistAgenda : AppRoutes("dentist_agenda")
    object PatientHome : AppRoutes("patient_home")
    object AttendanceControl : AppRoutes("attendance_control")
    object StaffList : AppRoutes("staffList")

    object StaffControl : AppRoutes("staff_control")
    object StaffControlHome : AppRoutes("staff_control_home")
    object DailyAttendance : AppRoutes("daily_attendance")
    object AbsenceCalendar : AppRoutes("absence_calendar")
    object ClockInScreen : AppRoutes("clock_in_screen")

    object StaffProfile : AppRoutes("staffProfile/{staffId}") {
        fun createRoute(staffId: Long) = "staffProfile/$staffId"
    }

    // --> AÑADIDO: RUTA PARA EDITAR EL TRABAJADOR
    object EditStaff : AppRoutes("editStaff/{staffId}") {
        fun createRoute(staffId: Long) = "editStaff/$staffId"
    }

    object ChatScreen : AppRoutes("chatScreen/{receiverId}") {
        fun createRoute(receiverId: Long) = "chatScreen/$receiverId"
    }
    object DoctorAgenda : AppRoutes("doctor_agenda/{doctorId}") {
        fun createRoute(doctorId: Long) = "doctor_agenda/$doctorId"
    }
    object DoctorPatients : AppRoutes("doctor_patients/{doctorId}") {
        fun createRoute(doctorId: Long) = "doctor_patients/$doctorId"
    }
    object StaffSchedule : AppRoutes("staffSchedule/{staffId}") {
        fun createRoute(staffId: Long) = "staffSchedule/$staffId"
    }
    object DoctorAvailability : AppRoutes("doctor_availability/{doctorId}") {
        fun createRoute(doctorId: Long) = "doctor_availability/$doctorId"
    }
    object ClinicalHome : AppRoutes("clinicalHome")
    object DoctorAvailabilityList : AppRoutes("doctorAvailabilityList")
}