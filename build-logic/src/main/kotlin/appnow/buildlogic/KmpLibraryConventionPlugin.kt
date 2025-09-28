package appnow.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")

        val compileSdkVersion = providers.gradleProperty("android.compileSdk").map(String::toInt).orElse(36)
        val minSdkVersion     = providers.gradleProperty("android.minSdk").map(String::toInt).orElse(24)

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget()

            iosArm64()
            iosSimulatorArm64()

            sourceSets.apply {
                // keep minimal; consumers add their own deps
                commonTest.dependencies {
                    implementation(kotlin("test"))
                }
            }
        }

        extensions.configure<LibraryExtension> {
            namespace = "com.appnow.${project.name}"
            compileSdk = compileSdkVersion.get()
            defaultConfig { minSdk = minSdkVersion.get() }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures { compose = false }
        }
    }
}