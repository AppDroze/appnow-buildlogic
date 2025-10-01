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

    private fun gp(key: String): String? = project.findProperty(key)?.toString()
    
    private fun gpDirect(key: String): String? = project.providers.gradleProperty(key).orNull

    private fun gpInt(key: String, fallback: Int): Int =
        gp(key)?.toIntOrNull() ?: fallback
        
    private fun gpIntDirect(key: String, fallback: Int): Int =
        gpDirect(key)?.toIntOrNull() ?: fallback

    private fun boolProp(key: String, default: Boolean = true): Boolean =
        gp(key)?.equals("true", ignoreCase = true) ?: default

    private fun detectAgpVersion(): String = runCatching {
        // AGP 8.x
        val c1 = Class.forName("com.android.Version")
        c1.getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null).toString()
    }.recoverCatching {
        // Older AGP
        val c2 = Class.forName("com.android.builder.model.Version")
        c2.getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null).toString()
    }.getOrDefault("unknown")

    private fun detectKotlinVersion(): String = runCatching {
        // Try to read from Kotlin plugin wrapper package version
        Class.forName("org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper")
            .`package`?.implementationVersion ?: "unknown"
    }.getOrDefault("unknown")

    private fun detectComposeVersion(): String = runCatching {
        // JetBrains Compose exposes this in the Gradle plugin jar
        Class.forName("org.jetbrains.compose.ComposeBuildConfig")
            .getField("composeVersion").get(null).toString()
    }.getOrDefault("unknown")

    private fun minAgpFor(compileSdk: Int): String = when {
        compileSdk >= 36 -> "8.4.0"
        compileSdk >= 35 -> "8.3.0"
        compileSdk >= 34 -> "8.2.0"
        else -> "8.0.0"
    }

    private fun compareVersions(a: String, b: String): Int {
        fun parse(v: String) = v.split('.', '-', '_').take(3).mapNotNull { it.toIntOrNull() }
        val pa = parse(a).padEnd(3); val pb = parse(b).padEnd(3)
        for (i in 0 until 3) {
            val d = pa[i].compareTo(pb[i])
            if (d != 0) return d
        }
        return 0
    }
    
    private fun List<Int>.padEnd(n: Int): List<Int> = this + List((n - size).coerceAtLeast(0)) { 0 }

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

    @TaskAction
    fun run() {
        println("🔍 AppNow Build Doctor\n" + "=".repeat(60))

        val compileSdk = gpInt("android.compileSdk", 36)
        val minSdk     = gpIntDirect("android.minSdk", 24)
        val targetSdk  = gpInt("android.targetSdk", 36)
        val minSupportedMinSdk = gpDirect("appnow.minSupportedMinSdk")?.toIntOrNull()
            ?: gpDirect("MIN_SUPPORTED_MIN_SDK")?.toIntOrNull()
            ?: 24

        val agpVer     = detectAgpVersion()
        val kVer       = detectKotlinVersion()
        val composeVer = detectComposeVersion()
        val useAndroidX = boolProp("android.useAndroidX", true)

        println("\n📋 Resolved Versions")
        println("  🔧 Plugins:  Kotlin=$kVer, AGP=$agpVer, Compose=$composeVer")
        println("  📦 AppNow:   catalog=${gp("appnow.catalogVersion") ?: gp("CATALOG_VERSION") ?: "unknown"}, plugin=${gp("appnow.versionName") ?: gp("VERSION_NAME") ?: "unknown"}")

        println("\n📱 Android SDK")
        println("  compileSdk=$compileSdk  minSdk=$minSdk  targetSdk=$targetSdk")
        println("  ✅ android.minSdk      = $minSdk")
        println("  ✅ policy.minSupported = $minSupportedMinSdk")

        if (project.findProperty("MIN_SUPPORTED_MIN_SDK") != null &&
            project.findProperty("appnow.minSupportedMinSdk") == null) {
            logger.warn("⚠️  Detected deprecated MIN_SUPPORTED_MIN_SDK. Switch to appnow.minSupportedMinSdk.")
        }

        // AndroidX check (fail-fast)
        if (!useAndroidX) {
            throw org.gradle.api.GradleException(
                """
                ❌ android.useAndroidX must be true.

                Fix: add to gradle.properties:
                  android.useAndroidX=true
                """.trimIndent()
            )
        } else {
            println("  ✅ AndroidX enabled (android.useAndroidX=true)")
        }

        // minSdk guard
        if (minSdk < minSupportedMinSdk) {
            throw org.gradle.api.GradleException("❌ android.minSdk=$minSdk is below supported minimum ($minSupportedMinSdk). " +
                "Increase module minSdk or lower the policy floor.")
        }

        // AGP vs compileSdk advisory (warning, not fatal)
        val requiredAgp = minAgpFor(compileSdk)
        if (agpVer != "unknown" && requiredAgp != "8.0.0") {
            val warn = compareVersions(agpVer, requiredAgp) < 0
            if (warn) {
                println("⚠️  AGP $agpVer may be too old for compileSdk=$compileSdk. Recommended AGP >= $requiredAgp.")
            } else {
                println("  ✅ AGP $agpVer is suitable for compileSdk=$compileSdk (>= $requiredAgp).")
            }
        }

        // Classpath sanity
        checkPluginClasses()

        println("\n" + "=".repeat(60))
        println("✅ Doctor check complete")
    }
}
