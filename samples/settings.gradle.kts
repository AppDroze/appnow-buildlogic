rootProject.name = "appnow-samples"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    includeBuild("../build-logic")
    includeBuild("../versioning")
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../catalog/gradle/libs.versions.toml"))
        }
    }
}

include(":showcase-lib", ":android-app")

