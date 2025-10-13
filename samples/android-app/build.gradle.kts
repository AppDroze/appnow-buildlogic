plugins {
    id("appnow.android.app")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.appnow.samples.androidapp"
    defaultConfig {
        applicationId = "com.appnow.samples.androidapp"
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":showcase-lib"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(libs.activity.compose)
}

