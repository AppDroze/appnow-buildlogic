rootProject.name = "appnow-buildlogic-workspace"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    includeBuild("versioning")
    includeBuild("build-logic")
}

// Show all builds in one IDE window
includeBuild("build-logic")
includeBuild("catalog")
includeBuild("versioning")
