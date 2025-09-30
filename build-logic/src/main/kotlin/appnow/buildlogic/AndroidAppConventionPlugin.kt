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

        // Load centralized configuration
        val config = BuildConfig.load(this)
        
        // Require SDK properties - fail with helpful error if missing
        val compileSdkProp = providers.gradleProperty("android.compileSdk").orNull
        val minSdkProp = providers.gradleProperty("android.minSdk").orNull
        val targetSdkProp = providers.gradleProperty("android.targetSdk").orNull

        if (compileSdkProp == null || minSdkProp == null || targetSdkProp == null) {
            throw GradleException("""
                Missing required Android SDK properties. Please add to your gradle.properties:
                
                android.compileSdk=${config.getProperty("android.compileSdk", "36")}
                android.minSdk=${config.getProperty("android.minSdk", "24")}
                android.targetSdk=${config.getProperty("android.targetSdk", "36")}
                
                These properties are required by the appnow.android.app convention plugin.
            """.trimIndent())
        }

        val compileSdkVersion = compileSdkProp.toInt()
        val minSdkVersion = minSdkProp.toInt()
        val targetSdkVersion = targetSdkProp.toInt()

        // Compatibility check - fail early if consumer uses unsupported SDK versions
        val minAllowed = BuildConfig.getIntValue(this, "MIN_SUPPORTED_MIN_SDK", 24)
        if (minSdkVersion < minAllowed) {
            throw GradleException("""
                android.minSdk=$minSdkVersion is below supported minimum ($minAllowed).
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