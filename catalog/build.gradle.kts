plugins {
    `version-catalog`
    `maven-publish`
    base // adds the standard `clean` task
}

import java.util.Properties

// Load centralized configuration
apply(from = "../build-config.gradle.kts")

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")

// Load version from centralized configuration
val configFile = file("../build-config.properties")
val properties = Properties()
if (configFile.exists()) {
    configFile.inputStream().use { properties.load(it) }
}
version = providers.gradleProperty("VERSION_NAME").getOrElse(properties.getProperty("VERSION_NAME", "0.2.3"))

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
}

tasks.register("verifyCatalog") {
    group = "verification"
    description = "Validates that the version catalog can be generated"
    dependsOn("generateCatalogAsToml")
}

publishing {
    repositories {
        // Only add remote repo when PUBLISH_URL is explicitly set
        val publishUrl = findProperty("PUBLISH_URL") as String? 
            ?: System.getenv("PUBLISH_URL")
        
        if (publishUrl != null) {
            maven {
                url = uri(publishUrl)
                credentials {
                    // For GH Packages, Gradle will pick these up from GitHub Actions automatically
                    username = findProperty("MAVEN_USER") as String?
                        ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = findProperty("MAVEN_TOKEN") as String?
                        ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
        // keep local for quick tests:
        mavenLocal()
    }
    
    publications {
        create<MavenPublication>("catalog") {
            from(components["versionCatalog"])
            pom {
                name.set("AppNow Shared Catalog")
                description.set("Centralized version catalog for AppNow projects")
            }
        }
    }
}
