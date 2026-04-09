#!/bin/bash
set -e

PROJECT_DIR="/home/administrator/.openclaw/workspace/sensitime-android"
SRC_DIR="$PROJECT_DIR/app/src/main"
BUILD_DIR="$PROJECT_DIR/tmt_build_final"
OUTPUT_APK="$PROJECT_DIR/sensitime_modified.apk"

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-35/android.jar"
AAPT2="/usr/bin/aapt2"
D8="/usr/bin/d8"
ZIPALIGN="/usr/bin/zipalign"
APKSIGNER="/usr/bin/apksigner"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/obj"
mkdir -p "$BUILD_DIR/apk"

echo "--- Step 1: Compiling Java Source (Targeting Java 8) ---"
javac -target 8 -source 8 -classpath "$ANDROID_JAR" \
      -d "$BUILD_DIR/obj" \
      "$SRC_DIR/java/com/example/sensitime/MainActivity.java" \
      "$SRC_DIR/java/com/example/sensitime/TimeService.java"

echo "--- Step 2: Converting Bytecode to Dex (D8) ---"
$D8 --release \
    --lib "$ANDROID_JAR" \
    --output "$BUILD_DIR/apk" \
    "$BUILD_DIR/obj/com/example/sensitime"/*.class

echo "--- Step 3: Packaging Resources and Manifest (AAPT2) ---"
$AAPT2 link \
    --manifest "$SRC_DIR/AndroidManifest.xml" \
    -I "$ANDROID_JAR" \
    --java src \
    -o "$BUILD_DIR/apk/base.apk"

cd "$BUILD_DIR/apk"
zip -q "base.apk" "classes.dex"
cd "$PROJECT_DIR"

echo "--- Step 4: Zipalign ---"
rm -f "$OUTPUT_APK"
$ZIPALIGN -v 4 "$BUILD_DIR/apk/base.apk" "$OUTPUT_APK"

echo "--- Step 5: Signing APK (Using temporary debug key) ---"
DEBUG_KEY="$PROJECT_DIR/temp_debug.jks"
rm -f "$DEBUG_KEY"
echo "86260486" | sudo -S keytool -genkey -v \
    -keystore "$DEBUG_KEY" \
    -alias debug_alias \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -storepass 123456 \
    -keypass 123456 \
    -dname "CN=SensiTime, OU=Dev, O=Example, L=Taipei, S=TW, C=TW" -noprompt

echo "86260486" | sudo -S $APKSIGNER sign --ks "$DEBUG_KEY" \
    --ks-pass pass:123456 \
    --out "$OUTPUT_APK" \
    "$OUTPUT_APK"

echo "Build Complete: $OUTPUT_APK"
