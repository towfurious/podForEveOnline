plugins {
    alias(libs.plugins.kotlin.multiplatform)  apply false
    alias(libs.plugins.kotlin.android)        apply false
    alias(libs.plugins.kotlin.serialization)  apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler)      apply false
    alias(libs.plugins.android.application)   apply false
    alias(libs.plugins.android.library)       apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.sqldelight)            apply false
    alias(libs.plugins.ktlint)                apply false
    alias(libs.plugins.detekt)                apply false
    alias(libs.plugins.kotlin.apple.privacy.manifests) apply false
    alias(libs.plugins.google.services)       apply false
    alias(libs.plugins.firebase.crashlytics)  apply false
}

// Capture version-catalog entry at root scope; `libs` is not available inside subprojects {}
val detektComposeRules = libs.detekt.compose.rules

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude { entry -> entry.file.toString().contains("/build/generated/") }
            exclude { entry -> entry.file.toString().contains("/build/generated") }
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        // Analyse all KMP source sets from each module
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/main/kotlin",
        )
    }

    dependencies {
        add("detektPlugins", detektComposeRules)
    }
}
