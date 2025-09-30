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
        val catVer = providers.gradleProperty("CATALOG_VERSION").getOrElse("0.0.0-LOCAL")

        // Prefer local catalog file when building inside the mono-repo/composite
        val localCatalog = file("../catalog/gradle/libs.versions.toml")
        create("buildlibs") {
            if (providers.gradleProperty("BUILDLOGIC_USE_LOCAL_CATALOG").orNull == "true" || localCatalog.exists()) {
                from(files(localCatalog))
            } else {
                from("com.appnow.build:appnow-catalog:$catVer")
            }
        }
    }
}

rootProject.name = "appnow-build-logic"
