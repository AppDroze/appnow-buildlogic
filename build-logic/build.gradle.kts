import java.util.Properties

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.buildlogic")

// Version precedence: ENV → -P → build-config.properties → default
val cfgFile = rootDir.resolve("..").resolve("build-config.properties")
val props = Properties().apply {
    if (cfgFile.exists()) cfgFile.inputStream().use { load(it) }
}
version = providers.environmentVariable("VERSION_NAME")
    .orElse(providers.gradleProperty("VERSION_NAME"))
    .orElse(props.getProperty("VERSION_NAME") ?: "")
    .getOrElse("0.3.0")

repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
    mavenCentral()
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
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
        val publishUrl = (findProperty("PUBLISH_URL") as String?)
            ?: System.getenv("PUBLISH_URL")
        if (publishUrl != null) {
            maven {
                url = uri(publishUrl)
                credentials {
                    username = (findProperty("MAVEN_USER") as String?)
                        ?: System.getenv("GITHUB_ACTOR").orEmpty()
                    password = (findProperty("MAVEN_TOKEN") as String?)
                        ?: System.getenv("GITHUB_TOKEN").orEmpty()
                }
            }
        }
        mavenLocal()
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AppNow Build Logic")
            description.set("Convention plugins for AppNow projects")
        }
    }
}
