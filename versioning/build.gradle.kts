plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.versioning")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.3.0")

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
