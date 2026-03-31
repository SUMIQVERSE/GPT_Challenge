plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.sanki.gallery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sanki.gallery"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Coil for efficient image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // ML Kit — uses pre-installed Google Play Services (zero extra APK size, offline)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:face-detection:16.1.7")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Coroutines Play Services (for .await() on ML Kit Tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // RunAnywhere SDK — On-Device VLM for "Chat with Gallery" premium feature
    implementation("io.github.sanchitmonga22:runanywhere-sdk-android:0.20.6")
    implementation("io.github.sanchitmonga22:runanywhere-llamacpp-android:0.20.6")
    // NOTE: ONNX module intentionally skipped (no STT/TTS needed, saves ~25MB)

    // Lifecycle Process Observer (for VLM model unload on background)
    implementation("androidx.lifecycle:lifecycle-process:2.6.2")
}
