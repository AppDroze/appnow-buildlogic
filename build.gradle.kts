tasks.register("cleanAll") {
    group = "workspace"
    dependsOn(
        gradle.includedBuild("build-logic").task(":clean"),
        // catalog has version-catalog plugin only - no build outputs to clean
    )
}

tasks.register("rebuildAll") {
    group = "workspace"
    description = "Publish catalog, then build convention plugins"
    dependsOn(
        // publish catalog so build-logic can resolve it from mavenLocal
        gradle.includedBuild("catalog").task(":publishToMavenLocal"),
        gradle.includedBuild("build-logic").task(":build")
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
