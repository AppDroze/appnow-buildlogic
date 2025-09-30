// Load centralized configuration
apply(from = "build-config.gradle.kts")

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
        val configFile = file("build-config.properties")
        val properties = java.util.Properties()
        
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        }
        
        println("📦 AppNow Build Logic Info")
        println("  Catalog Version: ${System.getenv("CATALOG_VERSION") ?: properties.getProperty("CATALOG_VERSION", "0.2.3")}")
        println("  Plugin Version:  ${System.getenv("VERSION_NAME") ?: properties.getProperty("VERSION_NAME", "0.2.3")}")
        println("  Compile SDK:     ${properties.getProperty("android.compileSdk", "36")}")
        println("  Min SDK:         ${properties.getProperty("android.minSdk", "24")}")
        println("  Target SDK:      ${properties.getProperty("android.targetSdk", "36")}")
        println("  Publish URL:     ${findProperty("PUBLISH_URL") ?: System.getenv("PUBLISH_URL") ?: "not set (mavenLocal only)"}")
    }
}
