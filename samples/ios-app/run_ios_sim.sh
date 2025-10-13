#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

# 1) Rebuild XCFramework for Apple Silicon simulator + device
echo "📦 Building XCFramework..."
./gradlew :showcase-lib:assembleXCFramework

# 2) Pick a simulator (prefer iPhone 15; fallback to any available iPhone)
DEVICE_NAME="${1:-iPhone 15}"
UDID=$(xcrun simctl list devices available | grep -m1 "$DEVICE_NAME (" | sed -E 's/.*\(([-0-9A-F]+)\).*/\1/' || true)
if [ -z "${UDID:-}" ]; then
  echo "⚠️  $DEVICE_NAME not found, trying any iPhone..."
  UDID=$(xcrun simctl list devices available | grep -m1 "iPhone" | sed -E 's/.*\(([-0-9A-F]+)\).*/\1/' || true)
fi

if [ -z "${UDID:-}" ]; then
  echo "❌ No iPhone simulator found. Please create one in Xcode."
  exit 1
fi

echo "📱 Using Simulator UDID: $UDID"
xcrun simctl boot "$UDID" 2>/dev/null || true

# 3) Build the Xcode project (Debug, Simulator)
echo "🔨 Building iOS app..."
pushd ios-app >/dev/null
xcodebuild -project SamplesIOSApp.xcodeproj \
  -scheme SamplesIOSApp \
  -configuration Debug \
  -destination "id=$UDID" \
  -derivedDataPath build \
  build 2>&1 | grep -E '(▸|error:|warning:|succeeded|failed)' || true
BUILD_RESULT=${PIPESTATUS[0]}
popd >/dev/null

if [ $BUILD_RESULT -ne 0 ]; then
  echo "❌ Build failed"
  exit 1
fi

# 4) Install & launch
APP_PATH="ios-app/build/Build/Products/Debug-iphonesimulator/SamplesIOSApp.app"
if [ ! -d "$APP_PATH" ]; then
  echo "❌ App not found at $APP_PATH"
  exit 1
fi

echo "📲 Installing $APP_PATH to simulator..."
xcrun simctl install "$UDID" "$APP_PATH"
echo "🚀 Launching app..."
xcrun simctl launch "$UDID" com.appnow.samples.iosapp

echo "✅ App launched successfully!"
echo "   Open Simulator.app to see it running."

