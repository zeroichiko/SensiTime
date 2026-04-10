# SensiTime v1.0 Release Notes

**Release Date:** 2026-04-10  
**Git Tag:** `v1.0-release`  
**Latest Commit:** `5bccbfb`

---

## 📦 APK Files

| File | Description |
|------|-------------|
| `sensitime_modified.apk` | Main build output (auto-generated) |
| `sensitime-v1.0-20260410.apk` | Versioned release copy |
| `temp_debug.jks` | Debug signing key (365 days validity) |

---

## ✅ v1.0 Features & Fixes

### Android 15 Compatibility
- ✅ Added `FOREGROUND_SERVICE_SPECIAL_USE` permission to Manifest
- ✅ Updated `startForeground()` to use three-parameter version (API 34+)
- ✅ Fixed notification visibility with `IMPORTANCE_DEFAULT`

### UI Improvements
- ✅ Speech Rate input field now accepts decimal values (`TYPE_NUMBER_FLAG_DECIMAL`)
- ✅ Android 13+ runtime notification permission request on service start
- ✅ More visible notification icon (`ic_lock_idle_low_battery`)

### Debug Support
- ✅ Added comprehensive logging in `TimeService.java` (tag: `SensiTime.Service`)
- ✅ Logcat diagnosis support for troubleshooting

---

## 📋 Git History (v1.0)

```
5bccbfb Fix: Use explicit TYPE_NUMBER_FLAG_DECIMAL constant for decimal input
73fc990 Fix Android 15: Add FOREGROUND_SERVICE_SPECIAL_USE permission + three-param startForeground() + debug logs
446dbe3 Fix: Make notification more visible (IMPORTANCE_DEFAULT) + Android 13+ runtime permission
4b673f6 Fix: Complete MainActivity with all helper methods and proper setContentView()
7a94c55 Fix: Complete MainActivity implementation with all helper methods
821e47e Initial commit: Rename project to SensiTime and clean up all old references
```

---

## 🔧 Build Instructions

```bash
./build_apk.sh
```

Output: `sensitime_modified.apk`

---

## 📱 Installation & Testing

1. Install APK on Android device (Android 15 recommended)
2. Grant notification permission when prompted
3. Click "Start Service" in the app
4. Verify persistent notification appears in status bar
5. Test decimal input in Speech Rate field (e.g., `1.5`)

---

## 🐛 Known Issues

- None at this release version

---

**Build completed:** 2026-04-10 09:59 GMT+8
