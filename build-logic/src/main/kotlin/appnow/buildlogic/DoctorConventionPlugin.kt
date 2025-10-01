package appnow.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class DoctorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
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
        println("=".repeat(60))

        printResolvedVersions()
        checkPluginClasses()
        checkAndroidProperties()
        warnAgpVsCompileSdk()
        requireAndroidX()

        println("=".repeat(60))
        println("✅ Doctor check complete")
    }

    private fun p(key: String, default: String? = null): String? =
        // prefer values exported by versioning plugin via extraProperties / gradle properties
        (project.findProperty(key) as? String) ?: default

    private fun printResolvedVersions() {
        println("\n📋 Resolved Versions:")

        val versionName   = p("appnow.versionName") ?: p("VERSION_NAME") ?: "unknown"
        val catalogVer    = p("appnow.catalogVersion") ?: p("CATALOG_VERSION") ?: versionName

        val agpVer        = agpVersion() ?: "unknown"
        val kotlinVer     = kotlinVersion() ?: "unknown"
        val composeVer    = composePluginVersion() ?: "unknown"

        println("  📦 Catalog Version: $catalogVer")
        println("  🔧 Plugin Version:  $versionName")
        println("  🧩 AGP:             $agpVer")
        println("  🧠 Kotlin:          $kotlinVer")
        println("  🎨 Compose MPP:     $composeVer")
    }

    private fun checkPluginClasses() {
        println("\n📦 Plugin Classes on Classpath:")
        val classes = mapOf(
            "Kotlin Multiplatform" to "org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPlugin",
            "Android Gradle Plugin" to "com.android.build.gradle.BasePlugin",
            "Compose Multiplatform" to "org.jetbrains.compose.gradle.ComposeMultiplatformPlugin",
        )
        classes.forEach { (name, fqcn) ->
            try {
                Class.forName(fqcn)
                println("  ✅ $name: Found")
            } catch (_: ClassNotFoundException) {
                println("  ❌ $name: Not found")
            }
        }
    }

    private fun checkAndroidProperties() {
        println("\n📱 Android SDK Properties:")
        val compileSdk = p("android.compileSdk", "36")!!
        val minSdk     = p("android.minSdk", "24")!!
        val targetSdk  = p("android.targetSdk", "36")!!

        println("  ✅ android.compileSdk = $compileSdk")
        println("  ✅ android.minSdk     = $minSdk")
        println("  ✅ android.targetSdk  = $targetSdk")

        val minSupported = (p("MIN_SUPPORTED_MIN_SDK", "24") ?: "24").toInt()
        val minSdkInt = minSdk.toIntOrNull()
        if (minSdkInt != null && minSdkInt < minSupported) {
            throw GradleException(
                """
                ❌ android.minSdk=$minSdkInt is below supported minimum ($minSupported).
                
                Fix: set at least:
                  android.minSdk=$minSupported
                in your gradle.properties.
                """.trimIndent()
            )
        }
    }

    private fun warnAgpVsCompileSdk() {
        val agp = agpVersion() ?: return
        val compileSdk = (p("android.compileSdk", "36") ?: "36").toIntOrNull() ?: return

        // simple guidance table (warn-only)
        // 33 → 8.0+, 34 → 8.1+, 35 → 8.2+, 36 → 8.3+
        val requiredAgpMinor = when {
            compileSdk >= 36 -> 3
            compileSdk >= 35 -> 2
            compileSdk >= 34 -> 1
            compileSdk >= 33 -> 0
            else -> null
        } ?: return

        val agpMinor = agp.split('.').getOrNull(1)?.toIntOrNull()
        if (agpMinor != null && agpMinor < requiredAgpMinor) {
            println(
                "⚠️  AGP $agp may be too old for compileSdk=$compileSdk. " +
                "Recommended AGP 8.$requiredAgpMinor+."
            )
        }
    }

    private fun requireAndroidX() {
        val useAndroidX = (p("android.useAndroidX") ?: "true").toBoolean()
        if (!useAndroidX) {
            throw GradleException(
                """
                ❌ android.useAndroidX must be true.
                
                Fix: add to gradle.properties:
                  android.useAndroidX=true
                """.trimIndent()
            )
        } else {
            println("\n✅ AndroidX enabled (android.useAndroidX=true)")
        }
    }

    // --- version helpers ---

    private fun agpVersion(): String? = try {
        val clazz = Class.forName("com.android.Version")
        clazz.getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)?.toString()
    } catch (_: Throwable) { null }

    private fun kotlinVersion(): String? = try {
        Class.forName("org.jetbrains.kotlin.config.KotlinCompilerVersion")
            .getField("VERSION").get(null)?.toString()
    } catch (_: Throwable) { null }

    private fun composePluginVersion(): String? = try {
        // Available in org.jetbrains.compose:compose-gradle-plugin
        Class.forName("org.jetbrains.compose.ComposeBuildConfig")
            .getField("pluginVersion").get(null)?.toString()
    } catch (_: Throwable) { null }
}
