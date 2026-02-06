# DEVELOPMENT.md

## Local Testing on Linux (No Physical Device)

### 1. Install Android Studio
```
# Download from https://developer.android.com/studio
# Or via snap:
sudo snap install android-studio --classic
```
- Launch: `android-studio`
- Install SDK (API 34+), Kotlin, Compose plugins

### 2. Setup Emulator (AVD)
```
# From Android Studio: Tools > AVD Manager
# Create Virtual Device:
# - Hardware: Pixel Tablet (10.95", tablet profile)
# - System: API 34 (Google Play or TV recommended for Gemini)
# OR Phone: Pixel 7 for mobile testing
```
- Start emulator: AVD Manager > Play ▶️
- Enable dev options: Settings > About tablet > Tap build 7x
- Wipe data if issues

### 3. ADB Commands
```
adb devices  # List emulators/devices
adb reverse tcp:8081 tcp:8081  # If using Metro (RN, not needed for native)
npx react-native log-android  # Logs (native: Android Studio Logcat)
```

### 4. Run Project
```
# From Android Studio: Run > Run 'app' (select emulator)
# Or CLI:
cd android
./gradlew installDebug  # Assumes connected emulator
```

### 5. Common Issues
- **No SDK**: `sdkmanager "platforms;android-34" "build-tools;34.0.0"`
- **Emulator slow**: Use hardware accel (KVM): `sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients virtinst`
- **Storage perms**: Emulator Settings > Apps > Special app access > Install unknown apps
- **Test tablet UI**: Emulator extended controls > Display > Density/DPI adjust

### 6. Debugging
- Logcat: Android Studio bottom panel
- Breakpoints: Standard
- Unit tests: `./gradlew testDebugUnitTest`
- UI tests: `./gradlew connectedCheck` (emulator running)

---
*Test all features on tablet emulator first.*