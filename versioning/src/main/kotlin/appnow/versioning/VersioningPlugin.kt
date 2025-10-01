package appnow.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.logging.Logging
import javax.inject.Inject
import java.io.IOException
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
        val logger = Logging.getLogger(VersioningPlugin::class.java)

        // Load build-config.properties from workspace root or current root
        val props = Properties()
        run {
            val candidates = listOf(
                project.rootProject.file("../build-config.properties"),
                project.rootProject.file("build-config.properties")
            )
            val f = candidates.firstOrNull { it.exists() }
            if (f != null) {
                try {
                    f.inputStream().use { props.load(it) }
                } catch (e: IOException) {
                    logger.warn("⚠️ appnow.versioning: failed to load ${f.absolutePath}: ${e.message}")
                }
            }
        }

        fun fromEnv(k: String): String? = System.getenv(k)
        fun fromProp(k: String): String? = project.findProperty(k)?.toString()
        fun fromCfg(k: String): String? = props.getProperty(k)

        fun str(key: String, fallback: String): String =
            fromEnv(key) ?: fromProp(key) ?: fromCfg(key) ?: fallback

        fun int(key: String, fallback: Int): Int =
            (fromEnv(key) ?: fromProp(key) ?: fromCfg(key))?.toIntOrNull() ?: fallback
            
        fun int(key: String, fallback: Int?): Int? =
            (fromEnv(key) ?: fromProp(key) ?: fromCfg(key))?.toIntOrNull() ?: fallback

        val versionName = str("VERSION_NAME", "0.0.1")
        val catalogVersion = str("CATALOG_VERSION", versionName)
        val compileSdk = int("android.compileSdk", 36)
        val minSdk = int("android.minSdk", 24)
        val targetSdk = int("android.targetSdk", 36)
        
        // Read namespaced first; legacy only for fallback
        val nsFloor = int("appnow.minSupportedMinSdk", null)
        val legacyFloor = int("MIN_SUPPORTED_MIN_SDK", null)

        val resolvedFloor = when {
            nsFloor != null && legacyFloor != null && nsFloor != legacyFloor -> {
                logger.warn("⚠️  MIN_SUPPORTED_MIN_SDK is deprecated. Both keys found; " +
                            "using appnow.minSupportedMinSdk=$nsFloor (legacy=$legacyFloor).")
                nsFloor
            }
            nsFloor != null -> nsFloor
            legacyFloor != null -> {
                logger.warn("⚠️  MIN_SUPPORTED_MIN_SDK is deprecated; use appnow.minSupportedMinSdk instead.")
                legacyFloor
            }
            else -> 24
        }

        ext.minSupportedMinSdk.set(resolvedFloor)

        // basic semver hint (non-fatal)
        val semverRegex = Regex("""^\d+\.\d+\.\d+(-[A-Za-z0-9.\-]+)?$""")
        if (!semverRegex.matches(versionName)) {
            logger.warn("⚠️ appnow.versioning: VERSION_NAME='$versionName' is not semver-like (X.Y.Z[-qualifier])")
        }

        // export as extras
        project.extensions.extraProperties.apply {
            set("appnow.versionName", versionName)
            set("appnow.catalogVersion", catalogVersion)
            set("android.compileSdk", compileSdk.toString())
            set("android.minSdk", minSdk.toString())
            set("android.targetSdk", targetSdk.toString())
            set("appnow.minSupportedMinSdk", ext.minSupportedMinSdk.get().toString())
            set("MIN_SUPPORTED_MIN_SDK", ext.minSupportedMinSdk.get().toString()) // legacy alias
        }

        // set project.version if unspecified
        val currentVersion = project.version.toString()
        if (currentVersion.equals("unspecified", ignoreCase = true) || currentVersion.isBlank()) {
            project.version = versionName
            logger.info("appnow.versioning: project.version set to $versionName")
        }
    }
}
