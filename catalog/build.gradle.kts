plugins {
    `version-catalog`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
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
