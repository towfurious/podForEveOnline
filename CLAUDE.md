# CLAUDE.md — PodForEve app repo

This repo is meant to be fully self-contained for working with Claude — checked out on any machine, with no machine-local Claude state (memory, personal skills, settings) required to pick up exactly where a previous session left off.

## Where things live

- **The vault** — `../podForEveOnline-vault` (sibling directory, its own git repo: `git@github.com:towfurious/podForEveOnline-vault.git`). This is the actual persistent memory of the project: every architecture decision (ADRs), domain quirk, and "why" behind the code. Read `podForEveOnline-vault/CLAUDE.md` for how it's organized and maintained. **Not optional** — a session that skips reading the vault is working blind on a project with over 20 ADRs of accumulated context.
- **Project skills** — `.claude/skills/` in this repo:
  - `selfcheck` — static lint/diff review before calling anything done.
  - `verify` — real device build/install/walkthrough for runtime-affecting changes.
  - `ship` — orchestrates both of the above, checks the vault's own dev-to-wiki table for owed documentation, and commits **both** this repo and the vault repo (separately — they're two different git repos).
- **CI** — `.github/workflows/ci.yml`.

## What does *not* live here (and why)

This project deliberately does not rely on Claude Code's user-level auto-memory (`~/.claude/projects/.../memory/`) for anything durable. That directory is machine-local, not git-tracked, and won't exist on a fresh checkout elsewhere — exactly the kind of state this project's whole setup is designed to avoid depending on. Two consequences:

- If you're tempted to save a "remember this for next time" fact about this project, it almost certainly belongs in the vault (an ADR, a concept page, a `log.md` entry) or in a `.claude/skills/*/SKILL.md` file instead — both are checked out with the repo, both survive a move to a new machine untouched.
- A `feedback`-type memory about this project existed briefly (`androidApp:lintDebug` is required verification for `androidMain` changes) before being folded into `selfcheck`'s own Step 1 and retired from `~/.claude` on 2026-07-24 — that's the intended pattern going forward, not a one-off cleanup.

## Quick start for a new session

1. Read this file (done, if you're reading it).
2. Read `../podForEveOnline-vault/CLAUDE.md`, then `../podForEveOnline-vault/index.md` to see what's already known.
3. Check `../podForEveOnline-vault/wiki/guides/Guide - App Store Launch Readiness.md` for current launch status.
4. Work normally; use `ship` when a unit of work is done instead of manually chaining lint/build/docs/commit.
