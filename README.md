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
- `android.minSdk` — your module's minSdk (consumer sets it)
- `appnow.minSupportedMinSdk` — policy floor (defaults to 24; configured centrally)

Builds fail if `android.minSdk < appnow.minSupportedMinSdk`.

## Structure
```
appnow-buildlogic/
├── build-logic/               # Convention plugins
├── catalog/                   # Version catalog
├── samples/                   # Sample projects
├── settings.gradle.kts        # Workspace config
└── build.gradle.kts          # Workspace tasks
```

## Samples

The `samples/` directory demonstrates end-to-end usage of the build logic:

- **showcase-lib**: KMP library (Android + iOS) using `appnow.kmp.library`
- **android-app**: Android app consuming the KMP library
- **ios-spm**: Swift Package Manager wrapper for iOS consumption

### Build & Run Samples

```bash
# 1. Build and publish build-logic locally
./gradlew publishLocal

# 2. Build the KMP library
cd samples
./gradlew :showcase-lib:build

# 3. Assemble Android app
./gradlew :android-app:assembleDebug

# 4. Publish library to mavenLocal
./gradlew :showcase-lib:publishToMavenLocal

# 5. Generate XCFramework for iOS
./gradlew :showcase-lib:assembleXCFramework
```

### iOS Integration

After building the XCFramework:

1. Open your iOS project in Xcode
2. File → Add Package → Add Local...
3. Select `samples/ios-spm/`
4. Import in Swift: `import ShowcaseLib`

The XCFramework will be at:
```
samples/showcase-lib/build/XCFrameworks/release/ShowcaseLib.xcframework
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
