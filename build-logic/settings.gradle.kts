import java.util.Properties

pluginManagement {
    // NOTE: mavenLocal() first on purpose for fast local iteration while developing included builds
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    includeBuild("../versioning")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    // NOTE: mavenLocal() first on purpose for fast local iteration while developing included builds
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("buildlibs") {
            // Resolve CATALOG_VERSION with precedence: ENV -> -P -> build-config.properties -> default
            val cfgFile = rootDir.resolve("..").resolve("build-config.properties")
            val props = Properties().apply {
                if (cfgFile.exists()) cfgFile.inputStream().use { load(it) }
            }
            val catVer = providers.environmentVariable("CATALOG_VERSION")
                .orElse(providers.gradleProperty("CATALOG_VERSION"))
                .orElse(props.getProperty("CATALOG_VERSION") ?: "")
                .getOrElse("0.3.0")

            // Workspace-friendly fallback: if local TOML exists, use it; otherwise use the published GAV
            val forcePublished = (System.getenv("APPNOW_FORCE_PUBLISHED") ?: "0") == "1"
            val localToml = rootDir.resolve("..").resolve("catalog/gradle/libs.versions.toml")

            if (!forcePublished && localToml.exists()) {
                from(files(localToml))
                println("🔎 build-logic: using local catalog TOML at $localToml")
            } else {
                from("com.appnow.build:appnow-catalog:$catVer")
                println("🔎 build-logic: using published catalog GAV com.appnow.build:appnow-catalog:$catVer")
            }
        }
    }
}

rootProject.name = "appnow-build-logic"
