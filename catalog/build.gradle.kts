plugins {
    id("appnow.versioning")
    `version-catalog`
    `maven-publish`
    base
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")
version = providers.gradleProperty("appnow.versionName").getOrElse("0.0.1")

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
