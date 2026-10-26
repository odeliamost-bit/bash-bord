plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "nl.odelia.dashboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "nl.odelia.dashboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2"
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
        // De versie voor de telefoon: geen testversie, dus niemand kan er met een kabel in kijken.
        release {
            isMinifyEnabled = false
            // Nog geen vaste sleutel? Dan met de test-sleutel tekenen, zodat hij toch te installeren is.
            signingConfig = signingConfigs.findByName("vast") ?: signingConfigs.getByName("debug")
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.glance.appwidget)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    // Op de telefoon zit org.json in Android zelf; voor de testjes op de computer deze echte versie.
    testImplementation(libs.org.json)
}
