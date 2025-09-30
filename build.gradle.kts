// Handy handles
val BL  = gradle.includedBuild("build-logic")
val CAT = gradle.includedBuild("catalog")

tasks.register("cleanAll") {
    group = "workspace"
    dependsOn(
        BL.task(":clean"),
        CAT.task(":clean"),
    )
}

tasks.register("buildAll") {
    group = "workspace"
    description = "Validate catalog, then build & check convention plugins"
    dependsOn(
        CAT.task(":generateCatalogAsToml"),
        BL.task(":build"),
        BL.task(":check"),
    )
}

tasks.register("publishLocal") {
    group = "workspace"
    description = "Publish catalog + plugins to mavenLocal"
    dependsOn(
        CAT.task(":publishToMavenLocal"),
        BL.task(":publishToMavenLocal"),
    )
}

tasks.register("publishRemote") {
    group = "workspace"
    description = "Publish catalog + plugins to the configured Maven repo (PUBLISH_URL)"
    dependsOn(
        CAT.task(":publish"),
        BL.task(":publish"),
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Clean, publish catalog locally, then build & check plugins"
    dependsOn("cleanAll", "publishLocal", "buildAll")
}

tasks.register("info") {
    group = "workspace"
    description = "Show current build/publish versions"
    doLast {
        val catalogVersion = providers.gradleProperty("CATALOG_VERSION").getOrElse("0.1.0")
        val versionName    = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")
        println("📦 AppNow Build Logic Info")
        println("  Catalog Version: $catalogVersion")
        println("  Plugin Version:  $versionName")
        println("  Publish URL:     ${findProperty("PUBLISH_URL") ?: System.getenv("PUBLISH_URL") ?: "not set (mavenLocal only)"}")
    }
}
