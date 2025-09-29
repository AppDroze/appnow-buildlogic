plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.buildlogic")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")

repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(buildlibs.kotlin.gradle.plugin)
    implementation(buildlibs.android.gradle.plugin)
    implementation(buildlibs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("appnowKmpLibrary") {
            id = "appnow.kmp.library"
            implementationClass = "appnow.buildlogic.KmpLibraryConventionPlugin"
            displayName = "AppNow KMP Library Convention"
            description = "Configures Kotlin Multiplatform library targets for AppNow"
        }
        register("appnowKmpCompose") {
            id = "appnow.kmp.compose"
            implementationClass = "appnow.buildlogic.KmpComposeConventionPlugin"
            displayName = "AppNow KMP Compose Convention"
            description = "Adds Compose Multiplatform defaults on top of KMP library"
        }
        register("appnowAndroidApp") {
            id = "appnow.android.app"
            implementationClass = "appnow.buildlogic.AndroidAppConventionPlugin"
            displayName = "AppNow Android App Convention"
            description = "Configures Android application defaults for AppNow"
        }
        register("appnowDoctor") {
            id = "appnow.doctor"
            implementationClass = "appnow.buildlogic.DoctorConventionPlugin"
            displayName = "AppNow Build Doctor"
            description = "Diagnose AppNow build configuration and plugin versions"
        }
    }
}

publishing {
    repositories {
        // Only add remote repo when PUBLISH_URL is explicitly set
        val publishUrl = findProperty("PUBLISH_URL") as String? 
            ?: System.getenv("PUBLISH_URL")
        
        if (publishUrl != null) {
            maven {
                url = uri(publishUrl)
                credentials {
                    // For GH Packages, Gradle will pick these up from GitHub Actions automatically
                    username = findProperty("MAVEN_USER") as String?
                        ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = findProperty("MAVEN_TOKEN") as String?
                        ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
        // keep local for quick tests:
        mavenLocal()
    }
    
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AppNow Build Logic")
            description.set("Convention plugins for AppNow projects")
        }
    }
}
