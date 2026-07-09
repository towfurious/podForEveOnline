plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "composeApp"
            isStatic = true
            // Re-export shared so the Xcode project only needs to import composeApp.framework.
            export(projects.shared)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared) // api = re-export transitive symbols to consumers
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.koin)
            implementation(libs.coil3.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.browser)
            implementation(libs.coil3.network.okhttp) // portrait image loading on Android
        }
        iosMain.dependencies {
            implementation(libs.coil3.network.ktor) // portrait image loading on iOS
        }
    }
}

android {
    namespace = "com.podforeve.tracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { compose = true }
}

// ComposeViewAdapter lives in ui-tooling (not ui-tooling-preview) — needed by AS preview renderer.
dependencies {
    debugImplementation(libs.compose.ui.tooling)
}
