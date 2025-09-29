package appnow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask

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
        
        val properties = mapOf(
            "android.compileSdk" to "36",
            "android.minSdk" to "24", 
            "android.targetSdk" to "36"
        )
        
        properties.forEach { (key, defaultValue) ->
            val value = project.findProperty(key) ?: defaultValue
            val isDefault = value == defaultValue
            val status = if (isDefault) "⚠️  (using default)" else "✅"
            println("  $status $key = $value")
        }
        
        // Check for dangerously low minSdk
        val minSdk = (project.findProperty("android.minSdk") as? String)?.toIntOrNull() ?: 24
        if (minSdk < 24) {
            println("  ❌ WARNING: android.minSdk=$minSdk is below supported minimum (24)")
        }
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)
