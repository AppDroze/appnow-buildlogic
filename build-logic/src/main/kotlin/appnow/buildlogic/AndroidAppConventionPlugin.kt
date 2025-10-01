package appnow.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

private const val DEFAULT_TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
private val DEFAULT_META_EXCLUDES = setOf("META-INF/AL2.0", "META-INF/LGPL2.1")

interface AppnowAndroidAppExtension {
    val enableMinify: Property<Boolean>
    val instrumentationRunner: Property<String>
}

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        // Create extension with sensible defaults
        val ext = extensions.create("appnowAndroidApp", AppnowAndroidAppExtension::class.java).apply {
            enableMinify.convention(false)
            instrumentationRunner.convention(DEFAULT_TEST_RUNNER)
        }

        // SDK properties with fallbacks from versioning plugin
        val compileSdkVersion = providers.gradleProperty("android.compileSdk").map(String::toInt).orElse(36).get()
        val minSdkVersion = providers.gradleProperty("android.minSdk").map(String::toInt).orElse(24).get()
        val targetSdkVersion = providers.gradleProperty("android.targetSdk").map(String::toInt).orElse(36).get()

        // Enforce minimum SDK requirement (AppNow policy: minSdk >= 24)
        if (minSdkVersion < 24) {
            throw GradleException("""
                android.minSdk=$minSdkVersion is below AppNow minimum (24).
                Please update android.minSdk to at least 24 in your gradle.properties.
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
                testInstrumentationRunner = ext.instrumentationRunner.get()
                vectorDrawables { useSupportLibrary = true }
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = ext.enableMinify.get()
                    if (ext.enableMinify.get()) {
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
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
                resources.excludes += DEFAULT_META_EXCLUDES
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