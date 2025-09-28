pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("buildlibs") {
            from(files("gradle/buildlibs.versions.toml"))
        }
    }
}
rootProject.name = "appnow-build-logic"
