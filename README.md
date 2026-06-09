# Resurrection+

Android Studio/Gradle project for **Resurrection+**, a highly optimized custom launcher.

## About Resurrection+

This is the **initial bringup** of Resurrection+, a custom launcher designed for speed, stability, and modern features. This project provides a fully buildable standalone base within Android Studio, serving as the foundation for our future development.

### Tooling & Versions
This project uses the latest stable development stack (as of June 2026):
- **Gradle:** `9.5.1`
- **Android Gradle Plugin (AGP):** `9.2.1`
- **Kotlin:** `2.4.0`
- **KSP:** `2.3.9`
- **Compile/Target SDK:** `37` (Android 17)
- **JDK:** `17+`

## Credits

- **Base Repository Credits:** This project was originally made possible by the incredible work in the [sestet/Launcher3](https://github.com/sestet/Launcher3) repository, which provided the foundation for a standalone Gradle-based build.

## Build

To build the project, use the following commands:

### Debug Build
```bash
./gradlew :assembleDebug
```
Output: `build/outputs/apk/resurrection/debug/Resurrection+-debug.apk`

### Release Build
```bash
./gradlew :assembleRelease
```
Output: `build/outputs/apk/resurrection/release/Resurrection+-release.apk`

## Install

```bash
# For Debug version
adb install -r build/outputs/apk/resurrection/debug/Resurrection+-debug.apk

# Start the Launcher
adb shell am start -W -n com.resurrection.launcher/.Launcher
```
