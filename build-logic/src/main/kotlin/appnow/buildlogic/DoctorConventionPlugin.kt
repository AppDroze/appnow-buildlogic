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
        
        // Check plugin classes on classpath
        checkPluginClasses()
        
        // Check Android SDK properties
        checkAndroidProperties()
        
        println("=" * 50)
        println("✅ Doctor check complete")
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
        
        val requiredProperties = listOf(
            "android.compileSdk" to "36",
            "android.minSdk" to "24", 
            "android.targetSdk" to "36"
        )
        
        val missingProperties = mutableListOf<String>()
        
        requiredProperties.forEach { (key, _) ->
            val value = project.findProperty(key)
            if (value == null) {
                println("  ❌ $key: Missing")
                missingProperties.add(key)
            } else {
                println("  ✅ $key = $value")
            }
        }
        
        // Fail if any required properties are missing
        if (missingProperties.isNotEmpty()) {
            val errorMessage = buildString {
                appendLine("❌ Missing required Android SDK properties:")
                missingProperties.forEach { key ->
                    val suggestedValue = when (key) {
                        "android.compileSdk" -> "36"
                        "android.minSdk" -> "24"
                        "android.targetSdk" -> "36"
                        else -> "<number>"
                    }
                    appendLine("  Add $key=$suggestedValue to your root gradle.properties.")
                }
            }
            throw GradleException(errorMessage)
        }
        
        // Check for dangerously low minSdk
        val minSdk = (project.findProperty("android.minSdk") as? String)?.toIntOrNull()
        if (minSdk != null && minSdk < 24) {
            throw GradleException("❌ android.minSdk=$minSdk is below supported minimum (24). Please update android.minSdk in your gradle.properties.")
        }
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)
