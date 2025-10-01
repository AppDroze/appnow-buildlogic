package appnow.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject
import java.util.Properties

open class AppnowVersioningExtension @Inject constructor(objects: ObjectFactory) {
    val versionName: Property<String> = objects.property(String::class.java)
    val catalogVersion: Property<String> = objects.property(String::class.java)
    val compileSdk: Property<Int> = objects.property(Int::class.java)
    val minSdk: Property<Int> = objects.property(Int::class.java)
    val targetSdk: Property<Int> = objects.property(Int::class.java)
    val minSupportedMinSdk: Property<Int> = objects.property(Int::class.java)
}

class VersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("appnowVersioning", AppnowVersioningExtension::class.java)
        val props = Properties()

        // Find the central file (works when applied inside included builds)
        val fileCandidates = listOf(
            project.rootProject.file("../build-config.properties"),
            project.rootProject.file("build-config.properties")
        )
        try {
            fileCandidates.firstOrNull { it.exists() }?.inputStream()?.use { props.load(it) }
        } catch (e: Exception) {
            project.logger.warn("⚠️ Failed to load build-config.properties: ${e.message}")
        }

        fun env(k: String) = System.getenv(k)
        fun gp(k: String) = project.findProperty(k)?.toString()
        fun cfg(k: String) = props.getProperty(k)

        fun v(key: String, fallback: String) =
            env(key) ?: gp(key) ?: cfg(key) ?: fallback

        fun vi(key: String, fallback: Int) =
            (env(key) ?: gp(key) ?: cfg(key))?.toIntOrNull() ?: fallback

        ext.versionName.set(v("VERSION_NAME", "0.0.1"))
        ext.catalogVersion.set(v("CATALOG_VERSION", ext.versionName.get()))
        ext.compileSdk.set(vi("android.compileSdk", 36))
        ext.minSdk.set(vi("android.minSdk", 24))
        ext.targetSdk.set(vi("android.targetSdk", 36))
        ext.minSupportedMinSdk.set(vi("MIN_SUPPORTED_MIN_SDK", 24))

        // Export as Gradle properties so build scripts can read without referencing the class
        project.extensions.extraProperties.apply {
            set("appnow.versionName", ext.versionName.get())
            set("appnow.catalogVersion", ext.catalogVersion.get())
            set("android.compileSdk", ext.compileSdk.get().toString())
            set("android.minSdk", ext.minSdk.get().toString())
            set("android.targetSdk", ext.targetSdk.get().toString())
            set("MIN_SUPPORTED_MIN_SDK", ext.minSupportedMinSdk.get().toString())
        }
    }
}
