plugins {
    id("appnow.versioning")
    id("appnow.publishing")
    `version-catalog`
    `maven-publish`
    base
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")
// No explicit `version = ...`; appnow.versioning sets it automatically.

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
