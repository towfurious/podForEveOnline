import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.apple.privacy.manifests)
}

// Replaces the old android.buildFeatures.buildConfig / buildConfigField mechanism (ADR-011).
// com.android.kotlin.multiplatform.library does not generate BuildConfig at all — it's
// variant-agnostic, and BuildConfig generation requires build types/flavors. A plain generated
// .kt file matches this project's existing preference for explicit, dependency-light secret
// plumbing (see ADR-011) rather than pulling in a plugin like BuildKonfig for one string constant.
val esiClientId: String = Properties().run {
    rootProject.file("local.properties").takeIf { it.exists() }?.let { load(it.inputStream()) }
    getProperty("esi.client_id", "")
}

val generateEsiConfig = tasks.register<GenerateEsiConfigTask>("generateEsiConfig") {
    clientId.set(esiClientId)
    outputDir.set(layout.buildDirectory.dir("generated/esiConfig/kotlin"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.podforeve.tracker.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // ic_notification.xml (src/androidMain/res/drawable) needs AAPT2 resource processing —
        // not on by default under this plugin.
        androidResources { enable = true }

        // Keeps commonTest (OAuthPkceTest, SkillProgressCalculatorTest, etc.) running against the
        // Android target, not just iOS via iosSimulatorArm64Test.
        withHostTestBuilder {}.configure {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // See wiki: [[Guide - App Store Launch Readiness]] (P0) and PrivacyInfo.xcprivacy's own comment
    // for which required-reason API categories are declared and why.
    privacyManifest {
        embed(privacyManifest = layout.projectDirectory.file("PrivacyInfo.xcprivacy").asFile)
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.koin.core)
        }
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.android.driver)
                implementation(libs.androidx.security.crypto)
                implementation(libs.androidx.core.ktx)
            }
            kotlin.srcDir(generateEsiConfig.flatMap { it.outputDir })
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.podforeve.tracker.db")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
            verifyMigrations.set(true)
        }
    }
}

// Generates shared/build/generated/esiConfig/kotlin/com/podforeve/tracker/auth/GeneratedEsiConfig.kt
// — the ESI_CLIENT_ID BuildConfig replacement. Output path deliberately sits under build/generated/
// so it's already covered by this repo's existing ktlint exclude filter (root build.gradle.kts),
// the same filter that already excludes SQLDelight's generated code.
abstract class GenerateEsiConfigTask : DefaultTask() {
    @get:Input
    abstract val clientId: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val pkgDir = outputDir.get().asFile.resolve("com/podforeve/tracker/auth")
        pkgDir.mkdirs()
        pkgDir.resolve("GeneratedEsiConfig.kt").writeText(
            """
            |package com.podforeve.tracker.auth
            |
            |// Generated by the :shared:generateEsiConfig Gradle task — do not edit by hand.
            |// Value comes from local.properties key "esi.client_id" (gitignored). See ADR-011.
            |internal object GeneratedEsiConfig {
            |    const val CLIENT_ID: String = "${clientId.get()}"
            |}
            |
            """.trimMargin(),
        )
    }
}
