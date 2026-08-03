# PodForEve

An unofficial companion app for *EVE Online* — Kotlin Multiplatform + Compose Multiplatform, targeting Android (shipping first) and iOS (in progress). Shows your character's skill queue, wallet, planetary interaction (PI), and industry jobs without opening the game client.

Private repo — this README is written for whoever's actually working on the code, not as a public project pitch.

## What it does

- **Skill Queue** — full training queue, active skill progress, time remaining.
- **Wallet** — recent ISK transactions with a running balance.
- **Planetary Interaction** — per-colony extractor countdown, factory status, storage fill.
- **Industry Jobs** — manufacturing/research/invention jobs from start to delivery.
- **Notifications** — skill/job/extractor completions, survives a device reboot.
- **Demo Mode** — every screen works with static sample data, no EVE account needed (see `store/listing.md` / vault ADR-022 for why this exists).
- Login via EVE Online's own SSO (OAuth2 PKCE) — this app never sees your password. No backend of its own; character data is cached on-device only.

## Module layout

```
shared/       KMP shared code — ESI networking (Ktor), domain models, repositories,
              auth (OAuth2 PKCE), SQLDelight cache, platform expect/actual
              (connectivity, secure storage, notifications).
composeApp/   Compose Multiplatform UI — screens, viewmodels, theme (5 color
              themes: 4 faction-inspired + AMOLED), Demo Mode sample data.
androidApp/   Android entry point — manifest, signing config, Firebase/Crashlytics
              wiring, release keystore (gitignored).
iosApp/       iOS Xcode project — SwiftUI shell hosting the Compose UI.
store/        Play Store listing assets (icon reference, feature graphic,
              category, short/full description copy).
```

Stack: Kotlin 2.4, Compose Multiplatform 1.11, AGP 9.2, Ktor, SQLDelight, Koin, Voyager, Coil3.

## Building

```bash
./gradlew androidApp:assembleDebug     # debug APK
./gradlew androidApp:bundleRelease     # release AAB — needs keystore.properties, see below
./gradlew composeApp:ktlintCheck shared:ktlintCheck composeApp:detekt shared:detekt
```

**Local setup** — add to `local.properties` (gitignored):
```properties
esi.client_id=<your EVE dev app client ID>   # from developers.eveonline.com
```

**Release signing** — needs a root `keystore.properties` (gitignored) pointing at a `.jks` keystore; see `ADR-011` and `ADR-001` (release signing) in the vault for the exact shape. Debug builds don't need any of this.

## Where things actually live

This repo is meant to be picked up on any machine via a plain git checkout — no machine-local Claude config or notes required. See `CLAUDE.md` for the full breakdown, but in short:

- **`../podForEveOnline-vault`** (sibling repo) — the real project memory: every architecture decision (30+ ADRs), domain quirks, ESI gotchas, and the launch-readiness tracker. Read this before assuming anything about *why* the code looks the way it does.
- **`.claude/skills/`** — `selfcheck` (lint + self-review), `verify` (real device build/install/walkthrough), `ship` (chains both + vault docs + commits both repos).
- **`.github/workflows/ci.yml`** — lint/detekt/test/build on every push, plus a manual release job.

## Legal

*EVE Online* and the EVE logo are trademarks of CCP hf. PodForEve is not affiliated with, endorsed by, or sponsored by CCP hf.
