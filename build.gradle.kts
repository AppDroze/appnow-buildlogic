tasks.register("cleanAll") {
    group = "workspace"
    dependsOn(
        gradle.includedBuild("build-logic").task(":clean"),
        gradle.includedBuild("catalog").task(":clean")
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Publish catalog, then build and check convention plugins"
    dependsOn(
        // publish catalog so build-logic can resolve it from mavenLocal
        gradle.includedBuild("catalog").task(":publishToMavenLocal"),
        gradle.includedBuild("build-logic").task(":build"),
        gradle.includedBuild("build-logic").task(":check")
    )
}

tasks.register("publishAllToMavenLocal") {
    group = "workspace"
    description = "Publish plugins & catalog to mavenLocal"
    dependsOn(
        gradle.includedBuild("catalog").task(":publishToMavenLocal"),
        gradle.includedBuild("build-logic").task(":publishToMavenLocal")
    )
}

tasks.register("buildAll") {
    group = "workspace"
    description = "Fast build without publishing (catalog validation + plugin build)"
    dependsOn(
        gradle.includedBuild("catalog").task(":generateCatalogAsToml"),
        gradle.includedBuild("build-logic").task(":build")
    )
}

tasks.register("smokeLocal") {
    group = "workspace"
    description = "Publish to mavenLocal for local testing"
    dependsOn(
        gradle.includedBuild("catalog").task(":publishToMavenLocal"),
        gradle.includedBuild("build-logic").task(":publishToMavenLocal")
    )
}

tasks.register("info") {
    group = "workspace"
    description = "Show current build/publish versions"
    doLast {
        val catalogVersion = providers.gradleProperty("CATALOG_VERSION").getOrElse("0.1.0")
        val versionName = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")
        println("📦 AppNow Build Logic Info")
        println("  Catalog Version: $catalogVersion")
        println("  Plugin Version:  $versionName")
        println("  Publish URL:     ${findProperty("PUBLISH_URL") ?: System.getenv("PUBLISH_URL") ?: "not set (mavenLocal only)"}")
    }
}

tasks.register("publishAll") {
    group = "workspace"
    description = "Publish catalog + plugins to the configured Maven repo"
    dependsOn(
        gradle.includedBuild("catalog").task(":publish"),
        gradle.includedBuild("build-logic").task(":publish")
    )
}
