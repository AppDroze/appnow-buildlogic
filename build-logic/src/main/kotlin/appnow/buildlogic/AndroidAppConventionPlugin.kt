package appnow.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        // SDK properties with fallbacks from versioning plugin
        val compileSdkVersion = providers.gradleProperty("android.compileSdk").map(String::toInt).orElse(36).get()
        val minSdkVersion = providers.gradleProperty("android.minSdk").map(String::toInt).orElse(24).get()
        val targetSdkVersion = providers.gradleProperty("android.targetSdk").map(String::toInt).orElse(36).get()
        val minSupportedMinSdk = providers.gradleProperty("MIN_SUPPORTED_MIN_SDK").map(String::toInt).orElse(24).get()

        // Compatibility check - fail early if consumer uses unsupported SDK versions
        if (minSdkVersion < minSupportedMinSdk) {
            throw GradleException("""
                android.minSdk=$minSdkVersion is below supported minimum ($minSupportedMinSdk).
                Please update android.minSdk in your gradle.properties.
            """.trimIndent())
        }

        // App identity properties are optional - only apply if present
        val appId = providers.gradleProperty("app.applicationId").orNull
        val appVersionCode = providers.gradleProperty("app.versionCode").orNull?.toIntOrNull()
        val appVersionName = providers.gradleProperty("app.versionName").orNull

        extensions.configure<ApplicationExtension> {
            compileSdk = compileSdkVersion

            defaultConfig {
                // Only set app identity properties if they exist
                appId?.let { applicationId = it }
                appVersionCode?.let { versionCode = it }
                appVersionName?.let { versionName = it }

                minSdk = minSdkVersion
                targetSdk = targetSdkVersion
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables { useSupportLibrary = true }
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            buildFeatures {
                // Compose toggled by consumer (or by KMP compose plugin on library modules)
                compose = false
            }

            packaging {
                resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
            }
        }

        // Kotlin toolchain and jvmTarget = 17 for app modules
        extensions.configure(KotlinAndroidProjectExtension::class.java) {
            jvmToolchain(17)
        }
        tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}