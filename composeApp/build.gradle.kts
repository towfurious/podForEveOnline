import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.podforeve.tracker"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
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
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.koin)
            implementation(libs.coil3.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.haze)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.browser)
            implementation(libs.coil3.network.okhttp) // portrait image loading on Android
            implementation(libs.compose.activity) // notification permission request launcher
        }
        iosMain.dependencies {
            implementation(libs.coil3.network.ktor) // portrait image loading on iOS
        }
    }
}

// ComposeViewAdapter lives in ui-tooling (not ui-tooling-preview) — needed by AS preview renderer.
// androidRuntimeClasspath replaces debugImplementation: this plugin has no build variants (no
// "debug"), so tooling-only deps go on the runtime classpath directly instead.
dependencies {
    "androidRuntimeClasspath"(libs.compose.ui.tooling)
}
