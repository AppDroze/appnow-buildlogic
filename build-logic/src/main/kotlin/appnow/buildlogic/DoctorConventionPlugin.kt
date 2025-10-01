package appnow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
class DoctorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Only apply to root project
        if (target != target.rootProject) return@with

        tasks.register("appnowDoctor", DoctorTask::class.java) {
            group = "appnow"
            description = "Diagnose AppNow build configuration and plugin versions"
        }
    }
}

abstract class DoctorTask : DefaultTask() {
    @TaskAction
    fun run() {
        println("🔍 AppNow Build Doctor")
        println("=" * 50)
        
        // Print resolved versions for CI sanity checks
        printResolvedVersions()
        
        // Check plugin classes on classpath
        checkPluginClasses()
        
        // Check Android SDK properties
        checkAndroidProperties()
        
        println("=" * 50)
        println("✅ Doctor check complete")
    }
    
    private fun printResolvedVersions() {
        println("\n📋 Resolved Versions:")
        
        val configFile = project.file("build-config.properties")
        val properties = java.util.Properties()
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        }
        
        val catalogVersion = System.getenv("CATALOG_VERSION")
            ?: project.findProperty("CATALOG_VERSION") as? String
            ?: properties.getProperty("CATALOG_VERSION", "0.3.0")
            
        val versionName = System.getenv("VERSION_NAME")
            ?: project.findProperty("VERSION_NAME") as? String
            ?: properties.getProperty("VERSION_NAME", "0.3.0")
        
        println("  📦 Catalog Version: $catalogVersion")
        println("  🔧 Plugin Version:  $versionName")
        println("  📱 Compile SDK:     ${properties.getProperty("android.compileSdk", "36")}")
        println("  📱 Min SDK:         ${properties.getProperty("android.minSdk", "24")}")
        println("  📱 Target SDK:      ${properties.getProperty("android.targetSdk", "36")}")
    }
    
    private fun checkPluginClasses() {
        println("\n📦 Plugin Classes on Classpath:")
        
        val pluginClasses = mapOf(
            "Kotlin Multiplatform" to "org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPlugin",
            "Android Gradle Plugin" to "com.android.build.gradle.BasePlugin",
            "Compose Multiplatform" to "org.jetbrains.compose.gradle.ComposeMultiplatformPlugin"
        )
        
        pluginClasses.forEach { (name, className) ->
            try {
                Class.forName(className)
                println("  ✅ $name: Found")
            } catch (e: ClassNotFoundException) {
                println("  ❌ $name: Not found")
            }
        }
    }
    
    private fun checkAndroidProperties() {
        println("\n📱 Android SDK Properties:")
        
        val compileSdk = project.providers.gradleProperty("android.compileSdk").orElse("36").get()
        val minSdk = project.providers.gradleProperty("android.minSdk").orElse("24").get()
        val targetSdk = project.providers.gradleProperty("android.targetSdk").orElse("36").get()
        val minSupportedMinSdk = project.providers.gradleProperty("MIN_SUPPORTED_MIN_SDK").orElse("24").get().toIntOrNull() ?: 24
        
        println("  ✅ android.compileSdk = $compileSdk")
        println("  ✅ android.minSdk = $minSdk")
        println("  ✅ android.targetSdk = $targetSdk")
        
        // Check for dangerously low minSdk
        val minSdkInt = minSdk.toIntOrNull()
        if (minSdkInt != null && minSdkInt < minSupportedMinSdk) {
            throw GradleException("❌ android.minSdk=$minSdkInt is below supported minimum ($minSupportedMinSdk). Please update android.minSdk in your gradle.properties.")
        }
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)
