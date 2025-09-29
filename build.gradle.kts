tasks.register("cleanAll") {
    group = "workspace"
    dependsOn(
        gradle.includedBuild("build-logic").task(":clean"),
        // catalog has version-catalog plugin only - no build outputs to clean
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Build both included builds"
    dependsOn(
        gradle.includedBuild("build-logic").task(":build"),
        gradle.includedBuild("catalog").task(":generateCatalogAsToml")
    )
}

tasks.register("publishAllToMavenLocal") {
    group = "workspace"
    dependsOn(
        gradle.includedBuild("build-logic").task(":publishToMavenLocal"),
        gradle.includedBuild("catalog").task(":publishToMavenLocal")
    )
}
