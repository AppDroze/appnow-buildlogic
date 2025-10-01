# AppNow Build Logic - Consumer Usage

## How to use AppNow Build Logic in your projects

### 1. Add the Version Catalog

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        // For GitHub Packages (private)
        maven { 
            url = uri("https://maven.pkg.github.com/AppDroze/appnow-buildlogic") 
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "<your-username>"
                password = System.getenv("GITHUB_TOKEN") ?: "<your-token>"
            }
        }
        google()
        mavenCentral()
        mavenLocal() // for local development
    }
    
    versionCatalogs {
        create("libs") {
            from("com.appnow.build:appnow-catalog:x.y.z") // Check build-config.properties for current version
        }
    }
}
```

### 2. Add the Convention Plugins

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        // Same repository configuration as above
        maven { 
            url = uri("https://maven.pkg.github.com/AppDroze/appnow-buildlogic")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "<your-username>"
                password = System.getenv("GITHUB_TOKEN") ?: "<your-token>"
            }
        }
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal() // for local development
    }
}
```

### 3. Use Convention Plugins

In your module's `build.gradle.kts`:

```kotlin
plugins {
    // For KMP libraries
    id("appnow.kmp.library") version "x.y.z" // Check build-config.properties for current version
    
    // For KMP libraries with Compose 
    id("appnow.kmp.compose") version "x.y.z" // Check build-config.properties for current version
    
    // For Android applications
    id("appnow.android.app") version "x.y.z" // Check build-config.properties for current version
}

// Configure Android app (optional)
appnowAndroidApp {
    enableMinify.set(true)  // Enable R8 for release builds (default: false)
    instrumentationRunner.set("androidx.test.runner.AndroidJUnitRunner")  // Customize test runner
}

// For Compose projects, add your dependencies:
dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    
    // Other dependencies from the catalog
    implementation(libs.kotlin.gradle.plugin)
    // etc.
}
```

### 4. Override Default Configuration (Optional)

In your project's `gradle.properties`:

```properties
# Override Android SDK versions
android.compileSdk=35
android.minSdk=28
android.targetSdk=35

# Override app configuration
app.applicationId=com.yourcompany.yourapp
app.versionCode=2
app.versionName=2.0.0

# Configure KMP iOS targets (for appnow.kmp.library)
# Disable iOS entirely
kmp.enableIos=false

# Or, customize iOS targets (default: arm64,simulatorArm64)
kmp.ios.targets=arm64,simulatorArm64,x64
```

### 5. Local Development

For local development with `mavenLocal()`, publish from the build-logic workspace:

```bash
# In appnow-buildlogic directory
./gradlew publishLocal
```

Then use the version from `build-config.properties` in your consumer projects.

### 6. Available Versions

- **Releases**: Check `build-config.properties` for current version (from GitHub releases/tags)
- **Snapshots**: Current version + `-SNAPSHOT` (from main branch pushes)
- **Local**: Current version (from local publishing)

### 7. Authentication

For GitHub Packages, you need:
- `GITHUB_ACTOR`: Your GitHub username
- `GITHUB_TOKEN`: Personal access token with `read:packages` permission

Set these as environment variables or in your `gradle.properties` (don't commit tokens!).
