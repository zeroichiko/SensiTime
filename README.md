# SensiTime - Android Proximity Time Announcer

A lightweight Android application that announces the current time via TTS when a hand approaches the proximity sensor while the screen is off and the device is charging.

## Features
- **Background Monitoring**: Runs as a foreground service to ensure reliability.
- **Charging Only Mode**: Triggers only during charging to save battery and avoid accidental triggers.
- **Customizable Settings**:
  - Proximity Threshold (cm)
  - Voice Volume (0-15)
  - Speech Rate (0.5 - 2.0)
  - Debounce Interval (seconds)
- **Dark Theme UI**: Black background with white text for minimal eye strain.

## Project Structure
- `app/src/main/AndroidManifest.xml`: App manifest and permissions.
- `app/src/main/java/com/example/sensitime/`: Core Java logic (`MainActivity`, `TimeService`).
- `build_apk.sh`: Automation script for compilation, dexing, alignment, and signing.

## Build Instructions
Run the provided build script:
\`\``bash build_apk.sh\`\`\`
