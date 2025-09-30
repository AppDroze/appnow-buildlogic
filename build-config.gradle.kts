// AppNow Build Logic - Centralized Configuration Loader
// This script loads configuration from build-config.properties

import java.util.Properties

/**
 * Loads configuration from build-config.properties file
 */
fun loadBuildConfig(): Properties {
    val configFile = file("build-config.properties")
    val properties = Properties()
    
    if (configFile.exists()) {
        configFile.inputStream().use { properties.load(it) }
    } else {
        // Fallback to default values if config file doesn't exist
        properties.setProperty("VERSION_NAME", "0.2.2")
        properties.setProperty("CATALOG_VERSION", "0.2.2")
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
fun getConfigValue(key: String, fallback: String = ""): String {
    return try {
        loadBuildConfig().getProperty(key, fallback)
    } catch (e: Exception) {
        fallback
    }
}

/**
 * Gets a configuration value as integer with fallback
 */
fun getConfigValueAsInt(key: String, fallback: Int = 0): Int {
    return try {
        getConfigValue(key, fallback.toString()).toInt()
    } catch (e: Exception) {
        fallback
    }
}

// Extension properties for easy access
extra["appnow.versionName"] = getConfigValue("VERSION_NAME", "0.2.1")
extra["appnow.catalogVersion"] = getConfigValue("CATALOG_VERSION", "0.2.1")
extra["appnow.compileSdk"] = getConfigValueAsInt("android.compileSdk", 36)
extra["appnow.minSdk"] = getConfigValueAsInt("android.minSdk", 24)
extra["appnow.targetSdk"] = getConfigValueAsInt("android.targetSdk", 36)
extra["appnow.minSupportedMinSdk"] = getConfigValueAsInt("MIN_SUPPORTED_MIN_SDK", 24)
