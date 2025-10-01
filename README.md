# AppNow Build Logic Workspace

This repo contains:
- **build-logic/**: convention plugins (publishable)
- **catalog/**: a published Gradle Version Catalog (publishable)

Open the **workspace root** in Android Studio; you'll see both as "Included builds".

## Quick Start

```bash
# Test the workspace
./gradlew rebuildAll

# Check project structure  
./gradlew -q projects

# Check current versions (no Gradle needed)
awk -F= '/^(VERSION_NAME|CATALOG_VERSION)=/{print $1": "$2}' build-config.properties
```

## How consumers use this

### A) Local dev (composite)
```kotlin
// consumer/settings.gradle.kts
pluginManagement {
  includeBuild("../appnow-buildlogic/build-logic")
}
```

Now consumer build.gradle.kts can:
```kotlin
plugins {
  id("appnow.kmp.library")
  id("appnow.kmp.compose")     // enables Compose, but you add deps
  id("appnow.android.app")
}

// For Compose projects, add your own dependencies:
dependencies {
  implementation(compose.runtime)
  implementation(compose.foundation)
  implementation(compose.material3)
  implementation(compose.ui)
  implementation(compose.components.resources)
}

// Configure Android app convention (optional)
appnowAndroidApp {
  // Enable R8/proguard for release builds (default: false)
  enableMinify.set(true)
  
  // Override test instrumentation runner (default shown)
  instrumentationRunner.set("androidx.test.runner.AndroidJUnitRunner")
}
```

### B) Published plugins
1. Add plugin descriptors to build-logic/build.gradle.kts
2. Publish: `./gradlew :build-logic:publishToMavenLocal`
3. Use with versions: `id("appnow.kmp.library") version "x.y.z"` (check build-config.properties for current version)

### C) Published version catalog
1. Add publishing to catalog/build.gradle.kts 
2. Publish: `./gradlew :catalog:publishToMavenLocal`
3. Use: `from("com.appnow.build:appnow-catalog:x.y.z")` (check build-config.properties for current version)

## Min SDK policy
- **android.minSdk** – your module's configured minSdk (consumer sets/overrides this).
- **appnow.minSupportedMinSdk** – policy floor defined by AppNow build logic. Builds fail if `android.minSdk` is lower.

Default floor: `24`. You can raise/lower the floor centrally in `build-config.properties`.

### Migration
`MIN_SUPPORTED_MIN_SDK` is deprecated. Use `appnow.minSupportedMinSdk`.
If both are present, the namespaced key wins.

## Structure
```
appnow-buildlogic/
├── build-logic/               # Convention plugins
├── catalog/                   # Version catalog
├── settings.gradle.kts        # Workspace config
└── build.gradle.kts          # Workspace tasks
```
# Trigger GitHub Actions
