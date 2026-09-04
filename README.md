# Dental Project - Android Frontend

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)

Aplicación móvil nativa para Android diseñada para la gestión eficiente de una clínica dental. Permite la administración de pacientes, citas y roles de usuario desde una interfaz fluida e intuitiva.

##  Contexto del Proyecto

Este repositorio es una evolución y mejora individual de un proyecto grupal realizado durante mis estudios de Desarrollo de Aplicaciones Multiplataforma (DAM). 

Puedes consultar la versión base original del trabajo en equipo aquí: [Dynalar_frontend_v1](https://github.com/MoonRodri/Dynalar_frontend_v1).

Decidí clonar el proyecto para refactorizar el código, aplicar mejores prácticas de desarrollo móvil y añadir nuevas funcionalidades por mi cuenta.

## Mis aportes y mejoras destacadas

En esta versión individual, me he centrado en mejorar tanto la experiencia de usuario (UX) como la seguridad y lógica interna de la app:

*   **Gestión de Roles y Seguridad:** Corrección de vulnerabilidades y mejora en el sistema de autenticación y permisos según el rol del usuario (Ej: Administrador vs Paciente).
*   **Filtros de Búsqueda Avanzados:** Implementación de un sistema de menú desplegable (dropdown) para optimizar la búsqueda y filtrado de pacientes en tiempo real.
*   **Refactorización de UI:** Mejoras generales en la interfaz para hacerla más limpia, responsiva y adaptada a los estándares de Material Design.
  

##  Tecnologías y Arquitectura

*   **Lenguaje:** Kotlin
*   **Arquitectura:** *(Ej: MVVM / MVC - ¡Añade el que hayas usado!)*
*   **UI:** *(Ej: XML Layouts o Jetpack Compose)*
*   **Librerías principales:**
    *   *(Ej: Retrofit para peticiones HTTP al backend)*
    *   *(Ej: Glide/Picasso para carga de imágenes)*
    *   *(Ej: Corrutinas para asincronía)*


##  Cómo ejecutar el proyecto

1. Clona este repositorio: `git clone https://github.com/Dianathecoder/dental-project-frontend.git`
2. Abre el proyecto en **Android Studio**.
3. Sincroniza el proyecto con los archivos de Gradle.
4. Ejecuta la app en un emulador o dispositivo físico (Android API XX+).
5. *Opcional:* Este frontend se conecta a su propia API. Asegúrate de tener corriendo el [dental-project-backend](https://github.com/Dianathecoder/dental-project-backend) localmente para probar todas las funciones.
