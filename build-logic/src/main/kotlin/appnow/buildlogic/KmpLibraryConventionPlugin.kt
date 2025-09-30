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

        // Require SDK properties - fail with helpful error if missing
        val compileSdkProp = providers.gradleProperty("android.compileSdk").orNull
        val minSdkProp = providers.gradleProperty("android.minSdk").orNull

        if (compileSdkProp == null || minSdkProp == null) {
            throw GradleException("""
                Missing required Android SDK properties. Please add to your gradle.properties:
                
                android.compileSdk=36
                android.minSdk=24
                
                These properties are required by the appnow.kmp.library convention plugin.
            """.trimIndent())
        }

        val compileSdkVersion = compileSdkProp.toInt()
        val minSdkVersion = minSdkProp.toInt()

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget()

            iosArm64()
            iosSimulatorArm64()

            // Consumers add their own dependencies and configure source sets as needed
        }

        extensions.configure<LibraryExtension> {
            compileSdk = compileSdkVersion
            defaultConfig { minSdk = minSdkVersion }

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