package appnow.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

class PublishingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Ensure maven-publish is present
        project.pluginManager.apply("maven-publish")

        val publishUrl = project.providers.gradleProperty("PUBLISH_URL")
            .orElse(project.providers.environmentVariable("PUBLISH_URL"))

        project.extensions.configure(PublishingExtension::class.java) {
            repositories {
                // Remote repo only if explicitly provided
                publishUrl.orNull?.let { url ->
                    maven {
                        name = "AppNowRemote"
                        setUrl(project.uri(url))
                        credentials {
                            username = (project.findProperty("MAVEN_USER") as String?)
                                ?: System.getenv("GITHUB_ACTOR") ?: ""
                            password = (project.findProperty("MAVEN_TOKEN") as String?)
                                ?: System.getenv("GITHUB_TOKEN") ?: ""
                        }
                    }
                }
                // Always keep local for quick tests
                mavenLocal()
            }
        }
    }
}
