> **Published.** Live at **https://towfurious.github.io/podforeve-privacy/** (separate public repo `towfurious/podforeve-privacy`, GitHub Pages — this file is the canonical source, keep both in sync on future edits). This copy in the private app repo is the working draft; the published page is what's actually linked from Play Console / App Store Connect.

# Privacy Policy for PodForEve

**Last updated:** July 24, 2026

PodForEve ("the app") is an unofficial companion app for *EVE Online*, a game published by CCP hf. PodForEve is not affiliated with, endorsed by, or sponsored by CCP hf.

## What data the app accesses

To show your character's skill queue, industry jobs, and planetary interaction (PI) status, PodForEve reads data directly from CCP's EVE Swagger Interface (ESI) — the official EVE Online API — after you sign in with your EVE Online account (OAuth2, via CCP's own login page). PodForEve never sees or stores your EVE Online password.

The data PodForEve reads includes: your character's name and portrait, security status, corporation, skill queue, wallet journal entries, industry jobs, and planetary colony/extraction status.

## Where your data is stored

**PodForEve has no server of its own.** There is no PodForEve-operated backend — the app talks directly to CCP's ESI servers over an encrypted connection, and CCP is the sole holder of your actual character data.

On your device, the app keeps:
- A local cache of the data above, so the app works instantly and doesn't need to refetch everything on every launch.
- Your OAuth refresh token, stored using your device's secure storage (Android EncryptedSharedPreferences / Android Keystore, or iOS Keychain) — never in plain text.

Nothing described above is transmitted to PodForEve's developer or to any third party. The only external party your data ever reaches is CCP hf, because you're using CCP's own API with your own EVE Online login.

## Data retention and deletion

Logging out of PodForEve clears your stored login session (the refresh token) and the local cache of character/skill/job/PI data described above. Nothing described above survives on the device after logout, other than by uninstalling the app itself.

## Analytics and tracking

PodForEve uses Firebase Crashlytics (Google) to collect crash reports and diagnostic logs when the app crashes, so problems can be found and fixed. This includes a device/installation identifier used to de-duplicate reports, sent to Google's Firebase servers over an encrypted connection. PodForEve does not use any advertising SDK and does not track you across other apps or websites for advertising purposes.

## Children's privacy

EVE Online requires players to be at least 16 years old (per CCP's own Terms of Service). PodForEve is not directed at children and does not knowingly collect data from anyone under that age.

## Your choices

You can revoke PodForEve's access to your EVE Online account at any time from CCP's [third-party application management page](https://developers.eveonline.com/authorized-apps) — this immediately invalidates PodForEve's access, independent of anything done inside the app itself.

## Changes to this policy

If this policy changes, the "Last updated" date above will change and, for material changes, we'll highlight it in the app.

## Contact

Questions about this policy: **viktor.shavarin.dev@gmail.com**
