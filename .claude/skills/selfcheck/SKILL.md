---
name: selfcheck
description: Static, no-app-launch second look at your own UNCOMMITTED changes in the PodForEve KMP/Compose Multiplatform repo — run this repo's ktlint/detekt tasks, then re-read the diff as a skeptical reviewer hunting for consistency bugs a first pass would miss. Use this whenever you (Claude) are about to say a coding task in this repo is done, whenever the user asks to "selfcheck", "self-check", "проверь свою работу", "просмотри диф", "прогони линтер и посмотри код", or similar — before offering to commit. This is NOT the same as the verify skill: selfcheck never builds, installs, or runs the app; it only reads code and runs lint/static-analysis Gradle tasks. If the user wants to know whether something actually works at runtime, that's verify, not this.
---

# selfcheck

A fast "did I just mess up my own fresh work" pass over uncommitted changes in this repo. Two steps, always both, in this order: lint, then a self-critical re-read of the diff. Neither step launches, builds an APK for, or drives the app — that's [[verify]]'s job, not this one.

## Why this exists

Writing code and reviewing code use different modes of attention. Right after writing something, you're in "does this do what I intended" mode — which is exactly the mode that misses "does this match what the *rest of the codebase* already does for the same concept." The bug that motivated this skill (`shared/.../SkillQueueRepository.kt` picking `queuePosition == 0` as "the active skill" instead of reusing the selector `DashboardViewModel.kt` already had) was invisible from reading the new file alone — the file that had the *correct* logic wasn't even part of the diff. Catching it required deliberately going and reading a file nobody had touched, specifically to compare.

So the second step below is not "read the diff again." It's: for every new business-rule decision in the diff, go find how the same concept is handled elsewhere — changed or not — and check they agree.

## Step 1 — Lint and static analysis

Run:
```
./gradlew composeApp:ktlintCheck shared:ktlintCheck composeApp:detekt shared:detekt
```

**If the diff touches anything under `shared/src/androidMain` (or is otherwise androidApp-visible), also run:**
```
./gradlew androidApp:lintDebug
```
This is not optional for those changes. [[ADR-016 - AGP KMP Library Plugin Migration]] moved `shared`/`composeApp` to a variant-agnostic AGP plugin that dropped their own Android-Lint-in-`check` integration — their `ktlint`/`detekt` say nothing about missing manifest permissions or other Lint-only findings. `androidApp` is a plain `com.android.application` and still has full Android Lint. On 2026-07-22 this exact gap let a real bug ship: `ConnectivityChecker.android.kt` called `ConnectivityManager.getActiveNetwork()` without `ACCESS_NETWORK_STATE` in the manifest — compiled clean, ktlint/detekt clean, unit tests green, and crashed fatally on every real-device launch. `androidApp:lintDebug`'s `MissingPermission` check catches this class of bug immediately; nothing else in this list does. See [[ADR-019 - Offline Detection]] for the full incident.

- If ktlint fails: run `./gradlew composeApp:ktlintFormat shared:ktlintFormat`, then re-run the check. Ktlint violations are formatting, not judgment calls — always auto-fix, never hand-edit to satisfy it.
- If detekt fails on something that's a deliberate, reasoned choice (e.g. a broad `catch (e: Exception)` that's intentionally catching "anything, because the caller must never see a failure from this" — see `NotificationScheduler.android.kt`'s `TooGenericExceptionCaught` suppression, or `SecureStorage.android.kt`'s `SwallowedException` suppression), add `@Suppress("RuleName")` with a comment explaining *why* the broad handling is correct here, not just that it's suppressed. If you can't articulate why in one sentence, it's probably a real finding — fix the code instead of suppressing.
- Don't move on to Step 2 until this is clean. A change that doesn't compile or lint clean isn't ready for a logic review yet.

## Step 2 — Self-critical diff re-read

Get the full picture of what changed:
```
git status
git diff HEAD          # or git diff <base>... if working on a branch
```

For uncommitted work this is normally small enough to hold in context at once — read all of it, not just the hunks that look interesting. Then go through the diff again looking specifically for these categories (roughly in order of how often they've actually caught something):

**1. New selectors/filters that duplicate an existing concept.** Any time the diff picks "the current X" out of a list, filters by a status, or decides which item is "active" — search the codebase for how that same concept is selected elsewhere, even in files the diff didn't touch. `grep`/`Explore` for the domain noun (e.g. the property or field name involved), not just the new function name. If two places compute "the active skill" / "the current job" / etc. and use different logic, one of them is wrong, and it might be the new one.

**2. Identifiers and keys that must match across call sites.** Anything used as a stable identity — notification ids/tags, cache keys, `PendingIntent` request codes, DB primary key components, string constants duplicated instead of shared — trace every place in the diff (and its call sites outside the diff) that constructs or compares that identity, and confirm they're building the exact same value. This project's dual-notification bug was exactly this: one code path posted to `(tag="skill_training_live", id=0)` and another to `(tag=null, id=1001)` — same intent, different key, two notifications instead of one.

**3. Exception/error handling boundaries.** For new try/catch blocks: does an exception thrown by *new* code get caught by an *old*, unrelated catch block higher up the call stack (silently swallowing it, possibly skipping code after it)? Conversely, does new code assume a call can't throw when it actually can (e.g. platform APIs that throw `SecurityException` under OS-version-specific restrictions)? A failure in a side-effecting helper (logging, scheduling, analytics) should almost never be able to abort the primary thing the caller was doing — check that it's isolated.

**4. Undocumented-but-real domain quirks.** This repo has an Obsidian vault at `../podForEveOnline-vault/` (sibling directory) documenting ESI shapes, business rules, and platform gotchas that aren't derivable from types alone (e.g. "ESI doesn't immediately rotate a finished skill-queue entry out of `queue_position == 0`," documented on the `Skill Queue` wiki page). If the diff touches a domain area with a vault entity/concept page, skim that page's "Business rules / invariants" section — a plausible-looking piece of code that contradicts a documented quirk is a real bug, not a style nitpick.

**5. The obvious stuff.** Off-by-one, null-safety assumptions that don't hold, copy-pasted blocks where only one instance got updated, resource leaks (unclosed things, unregistered receivers/listeners).

## Reporting

List findings most-severe-first. For each one:
- File + line
- One sentence: what's wrong
- One sentence: concrete input/state that breaks (not "this could theoretically be an issue" — an actual scenario)

If nothing survives this pass, say so plainly — don't manufacture a finding to seem thorough. If you found and already fixed something while reviewing (this project's convention — see `log.md` entries — is to fix-then-report, not report-then-wait), say what you fixed and why, the same way.

End with a one-line status: lint clean, N findings (M fixed / K reported), ready to hand off for [[verify]] or commit.
