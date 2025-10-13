plugins {
    id("appnow.kmp.library")
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "com.appnow.samples"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.datetime)
            }
        }
        val androidMain by getting
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
    
    // Configure iOS framework
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ShowcaseLib"
            isStatic = true
        }
    }
}

// XCFramework configuration
tasks.register("assembleXCFramework") {
    group = "build"
    description = "Assemble XCFramework for all iOS targets"
    
    dependsOn("linkReleaseFrameworkIosArm64", "linkReleaseFrameworkIosSimulatorArm64")
    
    doLast {
        val xcframeworkPath = file("build/XCFrameworks/release/ShowcaseLib.xcframework")
        xcframeworkPath.deleteRecursively()
        xcframeworkPath.parentFile.mkdirs()
        
        val iosArm64Framework = file("build/bin/iosArm64/releaseFramework/ShowcaseLib.framework")
        val iosSimulatorArm64Framework = file("build/bin/iosSimulatorArm64/releaseFramework/ShowcaseLib.framework")
        
        exec {
            commandLine(
                "xcodebuild", "-create-xcframework",
                "-framework", iosArm64Framework.absolutePath,
                "-framework", iosSimulatorArm64Framework.absolutePath,
                "-output", xcframeworkPath.absolutePath
            )
        }
        println("✅ XCFramework created at: ${xcframeworkPath.absolutePath}")
    }
}

