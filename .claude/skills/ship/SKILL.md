---
name: ship
description: Orchestrates the full close-out cycle for a unit of work in PodForEve — selfcheck, verify (when runtime-affecting), vault documentation, and committing both the app repo and the vault repo. Use whenever you (Claude) are about to declare a coding task finished, or whenever the user says "ship it", "шипни", "закроем это", "закоммить и обнови вики", "давай в vault закоммитим", or similar. This is the single command that chains selfcheck → verify → vault update → commit(s); don't run those three individually when the user asks for "ship" — run this instead.
---

# ship

The pattern this project actually uses, made explicit instead of re-derived from memory each time:

1. `selfcheck` — lint/detekt + self-critical diff re-read.
2. `verify` — real device build/install/walkthrough, but **only if the change is runtime-affecting** (UI, platform API, permission, notification, networking, anything a user would notice running the app). A pure refactor, a doc fix, or a Gradle-only change with no behavior change can skip this step — say so explicitly rather than running it reflexively.
3. Vault documentation — `podForEveOnline-vault/CLAUDE.md` §4.4 owns the actual rule table for what needs a wiki update; this skill doesn't restate it, it enforces that you actually checked it.
4. Commit the app repo.
5. Commit the vault repo **separately** — it is its own git repository with its own remote (`git@github.com:towfurious/podForEveOnline-vault.git`), not something that rides along inside the app repo's `.gitignore` (a stale claim in `podForEveOnline-vault/CLAUDE.md` §8 says otherwise — fix that line the next time you're in there, it's wrong).
6. Report what shipped, what's still open, and that neither repo was pushed (never push in this skill — pushing is its own explicit ask, every time, in both repos).

## Why this exists

Found 2026-07-24: an entire day's worth of real dev work (5 ADRs, several Guide updates, multiple log.md entries) was written correctly into the vault but never once committed to the vault's own git repo — every one of those edits sat as uncommitted changes while the *app* repo got clean, well-scoped commits the whole time. The habit of "update the vault" existed; the habit of "commit the vault" didn't. This skill exists so step 5 is never the one that gets silently dropped again.

## Steps in detail

### 1. `selfcheck`
Run it via the Skill tool (`selfcheck`), not by hand-copying its steps. If it finds something, fix it, then re-run until clean. Don't proceed to step 2 with known findings outstanding.

### 2. `verify` (conditional)
Run it via the Skill tool (`verify`) if the change touches anything a real device run could catch that static checks can't — this project already has two confirmed examples this month (a missing `ACCESS_NETWORK_STATE` permission, an `OfflineBanner` drawing behind the status bar) that `selfcheck` alone did not and could not catch. If you skip this step, say why in the final report ("no verify — pure Gradle/dependency change, no behavior difference") rather than silently omitting it.

### 3. Vault documentation
Ask, concretely, not rhetorically: for *this specific diff*, does any row in `podForEveOnline-vault/CLAUDE.md` §4.4's table apply? If yes — write the ADR / update the Guide status / update the concept or platform page / append `log.md` — before moving on. If a decision merits a new ADR, follow the vault's own numbering (check the highest existing `ADR-NNN` first, don't guess) and its append-only rule (new addendum or new ADR, never rewrite an Accepted one's original body).

### 4. Commit the app repo (`podForEveOnline`)
- `git status` / `git diff --stat` first — review what's actually staged.
- One commit per logical unit of work, not one mega-commit for the whole session, unless the user is explicitly clearing out a big backlog of unrelated prior work (as happened 2026-07-24 — 5 commits for 2 days of accumulated changes, split via `git add -p` where a single file mixed two concerns).
- Message: imperative summary line, body explains *why*, ends with the `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer per the global git-commit convention.

### 5. Commit the vault repo (`podForEveOnline-vault`) — separately, always
This is the step this skill exists to stop skipping. `cd` into the vault directory (it is a sibling of the app repo, e.g. `../podForEveOnline-vault` from `podForEveOnline/`), `git status`, and commit using **the vault's own established prefix convention** (see its `log.md`/`git log` for the exact style in use): `add:` (new page), `update:` (non-trivial edit to an existing page), `link:` (cross-reference added), `ingest:` (a source ingested), `lint:` (a lint pass), `meta:` (schema/index-only changes), `dev:` (wiki changes that rode along with a dev session — the common case when called from this skill). Small, frequent commits, per the vault's own §8 — don't bundle unrelated wiki changes into one commit if they're easy to separate.

### 6. Report
One short summary: what shipped (both repos, ideally with commit hashes), what's still open (P0/P1/P2 items, follow-ups), and an explicit "not pushed — say if you want it pushed" for both repos. Never push without being asked, every single time, even if the user pushed last time.
