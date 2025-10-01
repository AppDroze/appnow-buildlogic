// Handy handles
val BL  = gradle.includedBuild("build-logic")
val CAT = gradle.includedBuild("catalog")
val VER = gradle.includedBuild("versioning")

tasks.register("cleanAll") {
    group = "workspace"
    dependsOn(
        BL.task(":clean"),
        CAT.task(":clean"),
        VER.task(":clean"),
    )
}

tasks.register("buildAll") {
    group = "workspace"
    description = "Validate catalog, then build & check convention plugins"
    dependsOn(
        CAT.task(":generateCatalogAsToml"),
        BL.task(":build"),
        BL.task(":check"),
        VER.task(":build"),
    )
}

tasks.register("publishLocal") {
    group = "workspace"
    description = "Publish catalog + plugins to mavenLocal"
    dependsOn(
        CAT.task(":publishToMavenLocal"),
        BL.task(":publishToMavenLocal"),
        VER.task(":publishToMavenLocal"),
    )
}

tasks.register("publishRemote") {
    group = "workspace"
    description = "Publish catalog + plugins to the configured Maven repo (PUBLISH_URL)"
    dependsOn(
        CAT.task(":publish"),
        BL.task(":publish"),
        VER.task(":publish"),
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Clean, publish catalog locally, then build & check plugins"
    dependsOn("cleanAll", "publishLocal", "buildAll")
}

tasks.register("info") {
    group = "workspace"
    description = "Show current build/publish versions (config-cache friendly)"

    // Treat the config file as an optional input so config cache knows about it.
    val cfgFile = layout.projectDirectory.file("build-config.properties")
    inputs.file(cfgFile).optional()

    // Helper to read a property from the config file text via a Provider
    fun propFromFile(key: String) =
        providers.fileContents(cfgFile).asText.map { text ->
            val p = java.util.Properties()
            if (text.isNotBlank()) {
                java.io.StringReader(text).use { p.load(it) }
            }
            p.getProperty(key)
        }

    // Compose Providers with sensible fallbacks (env -> -P -> file -> default)
    val versionName = providers.environmentVariable("VERSION_NAME")
        .orElse(providers.gradleProperty("VERSION_NAME"))
        .orElse(propFromFile("VERSION_NAME"))
        .orElse("0.0.0")

    val catalogVersion = providers.environmentVariable("CATALOG_VERSION")
        .orElse(providers.gradleProperty("CATALOG_VERSION"))
        .orElse(propFromFile("CATALOG_VERSION"))
        .orElse(versionName)

    val compileSdk = providers.gradleProperty("android.compileSdk")
        .orElse(propFromFile("android.compileSdk"))
        .orElse("36")

    val minSdk = providers.gradleProperty("android.minSdk")
        .orElse(propFromFile("android.minSdk"))
        .orElse("24")

    val targetSdk = providers.gradleProperty("android.targetSdk")
        .orElse(propFromFile("android.targetSdk"))
        .orElse("36")

    val publishUrl = providers.gradleProperty("PUBLISH_URL")
        .orElse(providers.environmentVariable("PUBLISH_URL"))
        .orElse("not set (mavenLocal only)")

    doLast {
        println("📦 AppNow Build Logic Info")
        println("  Catalog Version: ${catalogVersion.get()}")
        println("  Plugin Version:  ${versionName.get()}")
        println("  Compile SDK:     ${compileSdk.get()}")
        println("  Min SDK:         ${minSdk.get()}")
        println("  Target SDK:      ${targetSdk.get()}")
        println("  Publish URL:     ${publishUrl.get()}")
    }
}
