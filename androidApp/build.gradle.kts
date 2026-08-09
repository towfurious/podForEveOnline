import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Release signing — see wiki: Guide - App Store Launch Readiness (P0 #1), mirrors ADR-011's
// local.properties pattern. keystore.properties and the .jks it points at are both gitignored;
// neither is ever committed. Debug builds and CI (which never builds a release artifact today)
// need neither file — only an actual release-producing task requires it, and fails loudly
// (rather than silently emitting an unsigned artifact) if it's missing.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

if (isReleaseBuild) {
    check(keystorePropertiesFile.exists()) {
        "Missing $keystorePropertiesFile — required to sign a release build. " +
            "See wiki: Guide - App Store Launch Readiness P0 #1 for the keystore.properties format."
    }
}
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.podforeve.tracker.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.podforeve.tracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // CI passes -PversionCode=<github.run_number> for a real android-release build (see
        // .github/workflows/ci.yml) so every Play Console upload gets a strictly-increasing
        // code with no manual bump. Local/debug builds fall back to 1 — Play Console never
        // sees those.
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures { compose = true }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.koin.android)
    implementation(libs.compose.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.browser)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
    debugImplementation(libs.compose.ui.tooling)
}
