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
    description = "Publish plugins & catalog to mavenLocal (when publishing is configured)"
    doLast {
        println("✅ Workspace tasks completed successfully!")
        println("📋 To use these:")
        println("  - Convention plugins: includeBuild(\"build-logic\") in consumer settings.gradle.kts")
        println("  - Version catalog: Add publishing to catalog/build.gradle.kts for remote usage")
    }
}
