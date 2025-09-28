plugins {
    `kotlin-dsl`
}

group = providers.gradleProperty("GROUP").getOrElse("com.appnow.buildlogic")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")

repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(buildlibs.kotlin.gradle.plugin)
    implementation(buildlibs.android.gradle.plugin)
    implementation(buildlibs.compose.gradle.plugin)
}
