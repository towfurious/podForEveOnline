---
name: verify
description: Real device/emulator smoke test for the PodForEve KMP/Compose Multiplatform app — build, install, launch, and drive the actual running app via adb to confirm a change works at runtime, not just that it compiles/lints/passes unit tests. Use this whenever you (Claude) are about to say a runtime-affecting change is done, whenever the user asks to "verify", "проверь на устройстве", "проверь визуально", "погоняй приложение", "проверь глазами", "пройдись по экранам", or similar. This is NOT the same as the selfcheck skill: selfcheck never builds, installs, or runs the app — it only reads code and runs lint/static-analysis Gradle tasks. If the user only wants a static code/diff review, that's selfcheck, not this.
---

# verify

A real on-device pass that drives the running app, not just a build that compiles. Two changes have already shipped that every static check (ktlint, detekt, unit tests, even `androidApp:lintDebug`) missed and this skill would have caught immediately:

1. `ConnectivityChecker` crashed the app on every launch — missing `ACCESS_NETWORK_STATE` — because it was never actually installed and opened on a device.
2. `OfflineBanner` drew its red background *behind* the status bar instead of below it — because nobody looked at a real screenshot with the banner actually visible.

Both were found only when the app was installed and used for real. That is the whole point of this skill.

## When to run it

- After any change that touches a real UI screen, a permission, a platform (`androidMain`/`iosMain`) API, notifications, or connectivity/network-layer behavior.
- Before telling the user a runtime-affecting change is "done" — pair with [[selfcheck]], don't substitute for it. `selfcheck` catches consistency bugs in the diff; this catches "does it actually run and look right."
- Whenever the user explicitly asks for it.

## Steps

### 1. Build and install
```
./gradlew androidApp:assembleDebug
adb devices -l   # confirm a real device or emulator is attached
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```
(`export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"` first if `adb` isn't already on `PATH` in the shell.)

### 2. Launch and confirm no crash
```
adb logcat -c
adb shell am force-stop com.podforeve.tracker
adb shell am start -n com.podforeve.tracker/com.podforeve.tracker.android.MainActivity
```
Wait a couple seconds, then check the process is still alive and nothing fatal was logged:
```
adb shell pidof com.podforeve.tracker
adb logcat -d --pid=$(adb shell pidof com.podforeve.tracker) | grep -iE "FATAL|AndroidRuntime|Exception|Error"
```
Screenshot to see what's actually on screen: `adb exec-out screencap -p > /tmp/screen.png`, then read it.

### 3. Walk the screens relevant to the change
Bottom-nav tabs are Dashboard / Skills / PI / Jobs — tap through whichever ones the change could plausibly affect (or all four, if the change touched something shared like theming, navigation, or a cross-cutting layer). Screenshot each. Look for: content actually rendering (not stuck on `Loading`/`Error`), no overlapping/clipped text, status bar and nav bar insets respected (see the `OfflineBanner` bug above — anything sitting outside a screen's own `Scaffold` needs its own `WindowInsets` handling).

Coordinates: `adb shell input tap <x> <y>` — `screencap` returns real device pixels; if reading the screenshot through a tool that displays it scaled down, multiply the on-screen estimate back up to real device pixels before tapping, and re-screenshot after each tap to confirm it landed before assuming the next state.

### 4. Exercise anything the change actually touched
Pick whichever of these are relevant — don't run all of them reflexively if the change was narrow:
- **Theming**: Dashboard → gear icon → Appearance → pick a non-default faction theme (e.g. Gallente, which has the documented `gainColor` amber-not-green special case — a good one to eyeball specifically). Confirm the whole screen re-themes, then switch back to what it was.
- **Connectivity**: `adb shell svc wifi disable && adb shell svc data disable`, wait ~5-6s (`ConnectivityObserver`'s poll interval), screenshot to confirm `OfflineBanner` appears correctly positioned; then `adb shell svc wifi enable && adb shell svc data enable`, confirm it disappears. Always restore network before finishing.
- **Notifications**: pull down the shade (`adb shell cmd statusbar expand-notifications`, collapse after with `adb shell input keyevent 4`) and confirm the expected notification is actually present with real data (e.g. the skill-training live countdown). Chucker's own notification in the same shade doubles as a quick real-network-traffic sanity check (e.g. a `304` response confirms `HttpCache` conditional requests are actually working against the real ESI server, not just in theory).
- **Login-dependent state** (logout cache wipe, boot-receiver notification reconciliation): needs a real EVE Online login on the test device first (`local.properties`'s `esi.client_id` must be filled in) — the user logs in themselves, Claude never enters EVE credentials.

### 5. Leave the device the way you found it
Restore network state, restore the theme if you changed it, force-stop or leave the app as appropriate. Don't leave a test device mid-experiment (airplane-mode-equivalent left on, wrong theme selected, etc.).

## Reporting

Say plainly what was actually driven on-device (which screens, which toggles) versus what's still only lint/test-level verified (e.g. "logout wipe not device-tested — no login on the connected device this pass"). If something looks wrong, fix it, re-verify on-device again before reporting done — don't report a screenshot-confirmed bug as fixed without re-screenshotting the fix.
