# HelloDarlin'

Cross-platform video calling that works in the browser and as an installable app.

## Web / PWA
- Live: https://nickatkinsbgky.github.io/crosscall/
- On mobile: open the site → browser menu → “Add to Home Screen” / “Install app”
- Service worker is included for basic offline caching and better installability

## Android App (WebView wrapper)
A minimal Android project lives in the `android/` folder. Open it in Android Studio to build an APK that loads the HelloDarlin' web app.

### Build steps
1. Open the `android/` folder in Android Studio
2. Sync Gradle
3. Run on a device or build a signed APK / AAB

The app loads `https://nickatkinsbgky.github.io/crosscall/` in a full-screen WebView with camera/mic permissions.

## Features
- Create room → share invite link
- Receiver opens link in any browser (no install required)
- Incoming call notifications (ringtone + overlay + browser notification) on both sides
- Mobile-optimized UI with safe areas, wake lock, touch controls
- Group video calls
