plugins {
    id("appnow.versioning")
    id("appnow.publishing")
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.buildlogic")
// No explicit `version = ...` here; appnow.versioning sets it automatically.

// NOTE: mavenLocal() first on purpose for fast local iteration while developing included builds
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

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
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
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("AppNow Build Logic")
            description.set("Convention plugins for AppNow projects")
        }
    }
}
