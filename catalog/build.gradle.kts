plugins {
    id("appnow.versioning")
    id("appnow.publishing")
    `version-catalog`
    `maven-publish`
    base
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")

// Resolve catalog version from appnow.versioning (extraProperties), with fallbacks
val catVersionFromExtras = (findProperty("appnow.versionName") as? String)    // we publish the catalog with the same x.y.z
val catVersionFromEnv    = System.getenv("VERSION_NAME")
val catVersionFromProp   = findProperty("VERSION_NAME") as? String

version = catVersionFromExtras
    ?: catVersionFromEnv
    ?: catVersionFromProp
    ?: "0.0.1"

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
