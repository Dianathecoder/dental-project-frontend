plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.dynalar_frontend_v1"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dynalar_frontend_v1"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- UI y Jetpack Compose (Base) ---
    // Agrupa todas las versiones de Compose para que no choquen entre sí
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)

    // Material 3: Componentes modernos (Cards, SearchBar, Buttons, etc.)
    implementation(libs.androidx.material3)
    // Iconos extra: Para tener acceso a flechas, lupa de búsqueda, etc.
    implementation("androidx.compose.material:material-icons-extended")

    // --- Navegación ---
    // Navigation Compose: Para moverte entre pantallas (Login, Home, Perfil)
    implementation(libs.androidx.navigation.compose)

    // --- Core y Utilidades ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Imágenes y Red ---
    // Coil: Cargar fotos desde internet (ej: fotos de perfil)
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Retrofit: Conectar con tu API o Base de Datos externa
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp: Gestiona las peticiones de red de forma eficiente
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.ui.unit)

    //implementation(libs.runtime)

    // --- Testing (Solo para pruebas) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("androidx.compose.material3:material3:1.2.0")

    // --Calendario
    implementation("com.kizitonwose.calendar:view:2.5.0")
// o si usas Compose:
    implementation("com.kizitonwose.calendar:compose:2.5.0")
// o la versión más reciente
}
