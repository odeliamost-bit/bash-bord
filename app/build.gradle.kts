plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "nl.odelia.dashboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "nl.odelia.dashboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    // Vaste handtekening, zodat een nieuwe versie over de oude heen gaat en je gegevens blijven.
    // De sleutel staat NIET in git: GitHub zet hem klaar uit een geheim (zie README).
    val sleutelPad = System.getenv("SLEUTEL_PAD")
    if (sleutelPad != null) {
        signingConfigs {
            create("vast") {
                storeFile = file(sleutelPad)
                storePassword = System.getenv("SLEUTEL_WACHTWOORD")
                keyAlias = "dashboard"
                keyPassword = System.getenv("SLEUTEL_WACHTWOORD")
            }
        }
    }

    buildTypes {
        debug {
            signingConfigs.findByName("vast")?.let { signingConfig = it }
        }
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
}
