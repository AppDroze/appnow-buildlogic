plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.versioning")

// Resolve version from environment (CI), then Gradle property, then fallback
val versionFromEnv = System.getenv("VERSION_NAME")
val versionFromProp = findProperty("VERSION_NAME") as? String

version = versionFromEnv
    ?: versionFromProp
    ?: "0.0.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("appnowVersioning") {
            id = "appnow.versioning"
            implementationClass = "appnow.versioning.VersioningPlugin"
            displayName = "AppNow Versioning"
            description = "Centralized version + SDK config loader for AppNow builds"
        }
    }
}

publishing {
    repositories {
        val publishUrl = findProperty("PUBLISH_URL") as String? ?: System.getenv("PUBLISH_URL")
        if (publishUrl != null) {
            maven {
                url = uri(publishUrl)
                credentials {
                    username = findProperty("MAVEN_USER") as String? ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = findProperty("MAVEN_TOKEN") as String? ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
        mavenLocal()
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AppNow Versioning")
            description.set("Centralized version + SDK config loader for AppNow builds")
        }
    }
}
