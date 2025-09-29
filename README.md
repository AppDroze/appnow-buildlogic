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
```

### B) Published plugins
1. Add plugin descriptors to build-logic/build.gradle.kts
2. Publish: `./gradlew :build-logic:publishToMavenLocal`
3. Use with versions: `id("appnow.kmp.library") version "0.1.0"`

### C) Published version catalog
1. Add publishing to catalog/build.gradle.kts 
2. Publish: `./gradlew :catalog:publishToMavenLocal`
3. Use: `from("com.appnow.build:catalog:0.1.0")`

## Structure
```
appnow-buildlogic/
├── build-logic/               # Convention plugins
├── catalog/                   # Version catalog
├── settings.gradle.kts        # Workspace config
└── build.gradle.kts          # Workspace tasks
```
