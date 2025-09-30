import java.util.Properties

plugins {
    `version-catalog`
    `maven-publish`
    base // adds the standard `clean` task
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")

// Version precedence: ENV → -P → build-config.properties → default
val cfgFile = rootDir.resolve("..").resolve("build-config.properties")
val props = Properties().apply {
    if (cfgFile.exists()) cfgFile.inputStream().use { load(it) }
}
version = providers.environmentVariable("VERSION_NAME")
    .orElse(providers.gradleProperty("VERSION_NAME"))
    .orElse(props.getProperty("VERSION_NAME") ?: "")
    .getOrElse("0.3.0")

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
        // Only add remote repo when explicitly provided
        val publishUrl = (findProperty("PUBLISH_URL") as String?)
            ?: System.getenv("PUBLISH_URL")
        if (publishUrl != null) {
            maven {
                url = uri(publishUrl)
                credentials {
                    username = (findProperty("MAVEN_USER") as String?)
                        ?: System.getenv("GITHUB_ACTOR").orEmpty()
                    password = (findProperty("MAVEN_TOKEN") as String?)
                        ?: System.getenv("GITHUB_TOKEN").orEmpty()
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
