pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        // If you'll use GH Packages for the catalog, add it here too.
        // maven { url = uri("https://maven.pkg.github.com/<org>/<repo>") ; credentials { ... } }
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        // Same note as above for GH Packages if needed
    }
    versionCatalogs {
        val catVer = providers.gradleProperty("CATALOG_VERSION").getOrElse("0.1.0")
        create("buildlibs") {
            from("com.appnow.build:appnow-catalog:$catVer")
        }
    }
}

rootProject.name = "appnow-build-logic"
