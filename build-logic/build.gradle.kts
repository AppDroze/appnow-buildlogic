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
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AppNow Build Logic")
            description.set("Convention plugins for AppNow projects")
        }
    }
}
