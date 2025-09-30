package appnow.buildlogic

import org.gradle.api.Project
import java.util.Properties

/**
 * Centralized configuration loader for AppNow Build Logic
 */
object BuildConfig {
    
    /**
     * Loads configuration from build-config.properties file
     */
    fun load(project: Project): Properties {
        val configFile = project.file("build-config.properties")
        val properties = Properties()
        
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        } else {
            // Fallback to default values if config file doesn't exist
            properties.setProperty("VERSION_NAME", "0.2.3")
            properties.setProperty("CATALOG_VERSION", "0.2.3")
            properties.setProperty("android.compileSdk", "36")
            properties.setProperty("android.minSdk", "24")
            properties.setProperty("android.targetSdk", "36")
            properties.setProperty("MIN_SUPPORTED_MIN_SDK", "24")
        }
        
        return properties
    }
    
    /**
     * Gets a configuration value with fallback
     */
    fun getValue(project: Project, key: String, fallback: String = ""): String {
        return load(project).getProperty(key, fallback)
    }
    
    /**
     * Gets a configuration value as integer with fallback
     */
    fun getIntValue(project: Project, key: String, fallback: Int = 0): Int {
        return try {
            getValue(project, key, fallback.toString()).toInt()
        } catch (e: Exception) {
            fallback
        }
    }
}
