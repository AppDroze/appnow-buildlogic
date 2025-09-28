package appnow.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // base KMP library setup
        pluginManager.apply("appnow.kmp.library")

        // Compose plugins
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        // If this module also uses Android library, flip compose on
        plugins.withId("com.android.library") {
            extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
            }
        }
    }
}