plugins {
    `version-catalog`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.build")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
}
