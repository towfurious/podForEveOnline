> **DRAFT — not yet reviewed or published.** Written 2026-07-16 as part of [Guide - App Store Launch Readiness](../podForEveOnline-vault/wiki/guides/Guide%20-%20App%20Store%20Launch%20Readiness.md) (P0 #4). Needs: your review of the wording, a contact address filled in below, and a hosting decision before it's linked from Play Console / App Store Connect.

# Privacy Policy for PodForEve

**Last updated:** [fill in on publish]

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

Logging out of PodForEve clears your stored login session (the refresh token) from the device. The local cache of character/skill/job/PI data described above currently remains on the device after logout, until you uninstall the app or clear the app's storage from your device's system settings.

<!-- TODO before publishing: consider making "log out" also wipe the local cache, so this section can say logout removes everything. Tracked as P1 #5 in the Guide. -->

## Analytics and tracking

PodForEve does not use any analytics or advertising SDK, and does not track you across apps or websites. [Update this section if a crash-reporting tool (e.g. Firebase Crashlytics or Sentry) is added later — see the Guide's P1 #4.]

## Children's privacy

EVE Online requires players to be at least 16 years old (per CCP's own Terms of Service). PodForEve is not directed at children and does not knowingly collect data from anyone under that age.

## Your choices

You can revoke PodForEve's access to your EVE Online account at any time from CCP's [third-party application management page](https://developers.eveonline.com/authorized-apps) — this immediately invalidates PodForEve's access, independent of anything done inside the app itself.

## Changes to this policy

If this policy changes, the "Last updated" date above will change and, for material changes, we'll highlight it in the app.

## Contact

Questions about this policy: **[fill in a contact email or repo URL]**
