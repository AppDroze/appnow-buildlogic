tasks.register("cleanAll") {
    group = "workspace"
    description = "Clean both included builds"
    dependsOn(
        gradle.includedBuild("build-logic").task(":clean")
        // catalog only has version-catalog plugin, no clean task needed
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Clean + assemble both included builds"
    dependsOn(
        gradle.includedBuild("build-logic").task(":build"),
        gradle.includedBuild("catalog").task(":generateCatalogAsToml")
    )
}

tasks.register("publishAllToMavenLocal") {
    group = "workspace"
    description = "Publish plugins & catalog to mavenLocal"
    dependsOn(
        gradle.includedBuild("build-logic").task(":publishToMavenLocal"),
        gradle.includedBuild("catalog").task(":publishToMavenLocal")
    )
}
