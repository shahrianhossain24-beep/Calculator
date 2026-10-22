# Calculator (com.android.claude.calculator)

Kotlin + Jetpack Compose. minSdk 27 (Android 8.1), target/compile 36. ABIs: armeabi-v7a, arm64-v8a, x86, x86_64.

## Build the APK
Local (needs JDK 17 + Android SDK):
    gradle wrapper --gradle-version 8.11.1
    ./gradlew testDebugUnitTest assembleDebug
    -> app/build/outputs/apk/debug/app-debug.apk

No local setup: push this folder to GitHub; the "Build APK" workflow runs the unit tests and uploads the APK as an artifact.

Instrumented UI tests (emulator/device): ./gradlew connectedDebugAndroidTest
