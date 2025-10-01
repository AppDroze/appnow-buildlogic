package appnow.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")

        // SDK properties with fallbacks from versioning plugin
        val compileSdkVersion = providers.gradleProperty("android.compileSdk").map(String::toInt).orElse(36).get()
        val minSdkVersion = providers.gradleProperty("android.minSdk").map(String::toInt).orElse(24).get()
        
        val minSdkValue = providers.gradleProperty("android.minSdk").map(String::toInt).orElse(24).get()
        val minSupportedMinSdk = providers.gradleProperty("appnow.minSupportedMinSdk")
            .map(String::toInt)
            .orElse(24)
            .get()

        require(minSdkValue >= minSupportedMinSdk) {
            "❌ android.minSdk=$minSdkValue is below supported minimum ($minSupportedMinSdk)."
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget()

            // Configuration knobs read from the consumer
            val enableIos = providers.gradleProperty("kmp.enableIos")
                .map { it.equals("true", ignoreCase = true) }
                .orElse(true) // default: true
                .get()

            val iosTargetsCsv = providers.gradleProperty("kmp.ios.targets")
                .orElse("arm64,simulatorArm64") // defaults: Apple Silicon host
                .get()
                .split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()

            if (enableIos) {
                if ("arm64" in iosTargetsCsv) iosArm64()
                if ("simulatorarm64" in iosTargetsCsv) iosSimulatorArm64()
                if ("x64" in iosTargetsCsv) iosX64() // opt-in for Intel simulators
            }

            // consumers add their own deps in sourceSets
        }

        extensions.configure<LibraryExtension> {
            compileSdk = compileSdkVersion
            defaultConfig { minSdk = minSdkValue }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures { compose = false }
        }

        // Ensure Kotlin compiles with JVM 17 as well
        tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}