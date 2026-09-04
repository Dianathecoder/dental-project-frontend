#  Dental Project - Android Frontend

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)

Aplicación móvil nativa para Android diseñada para la gestión eficiente de una clínica dental. Permite la administración de pacientes, citas y roles de usuario desde una interfaz fluida e intuitiva.

##  Contexto del Proyecto

Este repositorio es una evolución y mejora individual de un proyecto grupal realizado durante mis estudios de Desarrollo de Aplicaciones Multiplataforma (DAM). 

Puedes consultar la versión base original del trabajo en equipo aquí: [Dynalar_frontend_v1](https://github.com/MoonRodri/Dynalar_frontend_v1).

Decidí clonar el proyecto para refactorizar el código, aplicar mejores prácticas de desarrollo móvil y añadir nuevas funcionalidades por mi cuenta.

##  Mis aportes y funcionalidades destacadas

En esta versión individual, he rediseñado la arquitectura lógica de la aplicación para convertirla en un sistema integral de gestión de clínicas con control de acceso basado en roles (RBAC). 

###  Sistema de Autenticación y Seguridad
*   **Inicio de sesión híbrido:** Implementación de acceso mediante Google Login y un flujo de registro controlado.
*   **Gestión de contraseñas seguras:** El Administrador genera contraseñas temporales de primer acceso para el personal médico, mientras que los pacientes tienen un flujo de acceso directo y permanente.

###  Funcionalidades específicas por Rol (En desarrollo)
He implementado una separación estricta de permisos e interfaces dependiendo del tipo de usuario:

*   ** Administrador (Control Total):**
    *   Creación y gestión de todos los usuarios (Doctores, Auxiliares, Pacientes).
    *   Configuración de horarios de trabajo y sistema de fichaje para empleados.
    *   Gestión clínica integral: Odontogramas, inventario y logística (boxes, protocolos, materiales).
    *   Administración completa de pacientes (creación, borrado, perfiles y gestión documental) y calendario.
*   ** Auxiliar (Gestión Operativa):**
    *   Acceso a todas las herramientas clínicas, calendario y gestión de pacientes del Administrador.
    *   Capacidad de registrar su entrada/salida (fichaje) en el horario establecido.
    *   *Restricción:* Sin permisos de creación de usuarios o configuración global.
*   ** Doctor (Enfoque Clínico):**
    *   Interfaz enfocada en su jornada: visualización de citas propias, box (sala) asignado y acceso directo a las fichas de los pacientes que le tocan.
    *   Visualización del calendario general (modo lectura, sin permisos de modificación).
*   ** Paciente (Portal de Usuario):**
    *   Autogestión de citas: solicitud de nuevas visitas.
    *   Visualización de los detalles de su cita programada (box asignado y doctor responsable).

###  Chat Interno Segmentado (En desarrollo)
Estoy implementando un sistema de mensajería en tiempo real con reglas de comunicación estrictas para mantener el flujo de trabajo organizado:
*   **Administrador:** Se comunica con todo el personal (Doctores y Auxiliares).
*   **Auxiliar:** Funciona como centralita; se comunica con todos los roles (Admin, Doctores y Pacientes).
*   **Doctor:** Se comunica exclusivamente con el staff interno (Admin y Auxiliares).
*   **Paciente:** Canal directo de atención exclusivo con los Auxiliares.

###  Diseño, UX e Internacionalización
*   **Soporte Multilenguaje:** Interfaz completamente internacionalizada (i18n) con soporte dinámico para **Español, Catalán e Inglés**, permitiendo a los usuarios utilizar la app en su idioma de preferencia.
*   **Refactorización de Interfaz:** Renovación completa de la UI para hacerla más accesible, intuitiva y adaptada a las necesidades reales del día a día en una clínica, utilizando componentes modernos.

##  Tecnologías y Arquitectura

El ecosistema de esta aplicación está dividido en dos partes: este cliente móvil nativo y una API REST dedicada.

### Frontend (Este repositorio)
*   **Lenguaje:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Desarrollo de UI declarativa y moderna)
*   **Arquitectura:** MVVM (Model-View-ViewModel) para separar la lógica de negocio de la interfaz visual.
*   **Librerías principales:**
    *   **Retrofit:** Para el consumo de la API y las peticiones HTTP.
    *   **Corrutinas (Coroutines) y Flow:** Para la gestión de tareas asíncronas y flujos de datos reactivos.
    *   **Coil:** Para la carga eficiente de imágenes asíncronas en Compose.

### Backend
*   **Tecnología Base:** Java con el framework Spring Boot.
*   **Repositorio:** Puedes consultar el código fuente del servidor aquí: [dental-project-backend](https://github.com/Dianathecoder/dental-project-backend)

##  Cómo ejecutar el proyecto

1. Clona este repositorio: `git clone https://github.com/Dianathecoder/dental-project-frontend.git`
2. Abre el proyecto en **Android Studio**.
3. Sincroniza el proyecto con los archivos de Gradle.
4. Ejecuta la app en un emulador o dispositivo físico (Android API 24 o superior).
5. *Opcional:* Este frontend se conecta a su propia API. Asegúrate de tener corriendo el [dental-project-backend](https://github.com/Dianathecoder/dental-project-backend) localmente para probar todas las funciones.
