plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.evobi.posefindernative"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.evobi.posefindernative"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    val camerax_version = "1.5.2"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 2. The Core library (you likely already have this)
    implementation("androidx.camera:camera-core:${camerax_version}")

    // 3. THE FIX: The actual Camera2 implementation (Missing this causes your crash!)
    implementation("androidx.camera:camera-camera2:${camerax_version}")

    // 4. Lifecycle and View components (Required for ProcessCameraProvider and PreviewView)
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")


    // If you want to use the base sdk
    implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
    // If you want to use the accurate sdk
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")
}