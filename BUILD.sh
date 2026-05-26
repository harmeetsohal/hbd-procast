#!/bin/bash
##############################################################
# HBD PROCAST — One-Command APK Builder
# Run this script on any machine with Android Studio installed
##############################################################
set -e

echo "=================================================="
echo "  HBD PROCAST ProBill — APK Builder"
echo "  Version 1.0.0"
echo "=================================================="

# Auto-detect Android SDK
if [ -z "$ANDROID_HOME" ]; then
  if [ -d "$HOME/Library/Android/sdk" ]; then
    export ANDROID_HOME="$HOME/Library/Android/sdk"
  elif [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
  elif [ -d "/opt/android-sdk" ]; then
    export ANDROID_HOME="/opt/android-sdk"
  else
    echo "ERROR: Android SDK not found. Please install Android Studio."
    echo "Then set: export ANDROID_HOME=/path/to/android/sdk"
    exit 1
  fi
fi

echo "✓ Android SDK: $ANDROID_HOME"

# Create local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "✓ local.properties configured"

# Make gradlew executable
chmod +x gradlew

echo ""
echo "Building DEBUG APK..."
./gradlew assembleDebug

echo ""
echo "Building RELEASE APK..."
./gradlew assembleRelease

APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE="app/build/outputs/apk/release/app-release-unsigned.apk"

echo ""
echo "=================================================="
echo "  BUILD COMPLETE!"
echo "=================================================="
if [ -f "$APK_DEBUG" ]; then
  echo "  Debug APK:   $APK_DEBUG"
  echo "  Size: $(du -sh $APK_DEBUG | cut -f1)"
fi
if [ -f "$APK_RELEASE" ]; then
  echo "  Release APK: $APK_RELEASE"
fi
echo "=================================================="
echo ""
echo "To install on connected Android device:"
echo "  $ANDROID_HOME/platform-tools/adb install $APK_DEBUG"
