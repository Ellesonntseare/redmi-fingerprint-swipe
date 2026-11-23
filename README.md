# Fingerprint Swipe

An Android app for testing whether a Redmi 15 reports fingerprint swipe gestures.
A downward swipe requests Android's notification panel. The app has no network
permission and does not request access to notification contents, app screens,
or fingerprint data.

## Test on your phone

1. Install FingerprintSwipe.apk and open Fingerprint Swipe.
2. Tap **Open accessibility settings**. Find **Fingerprint Swipe** under downloaded apps
   and turn the service on. If Android blocks the setting for this sideloaded app,
   open **Settings → Apps → Fingerprint Swipe → ⋮ → Allow restricted settings**
   first, if that option is available.
3. Return to the app and tap **Test notification panel**. This tests the panel action
   separately from the sensor. Close the panel when it opens.
4. With your phone unlocked, slide your finger down the side fingerprint sensor
   without pressing it. Return to the app if needed to check **Last swipe**.
5. If no swipe is recorded after several tries while unlocked and outside biometric
   prompts, the phone may not expose swipe events through Android's accessibility API.

You can disable the gesture anytime under Accessibility → Fingerprint Swipe.

## Build

Open the root folder in Android Studio, or install Java 17, Gradle 8.9,
and Android SDK platform 35, then run:

```sh
gradle :app:assembleDebug :app:lintDebug
```

The signed debug APK is at `app/build/outputs/apk/debug/app-debug.apk`.
In GitHub, the **Build APK** workflow runs on push and uploads the APK under
the run's **Artifacts**. The workflow uses a debug signing key local to the runner.
If a later build uses a different key, uninstall the previous build before installing.
