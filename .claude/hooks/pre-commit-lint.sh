#!/usr/bin/env bash
# PreToolUse hook (matcher: Bash). Gates `git commit` behind ktlint + detekt.
# No `set -e`: we need the real exit code of the gradle run below, and
# `set -e` would abort the script the instant `output=$(...)` fails,
# before we ever get to read $status.

PROJECT_ROOT="/Users/vshavarin/Developer/PodForEve/podForEveOnline"

input=$(cat)
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')

case "$command" in
  *"git commit"*) ;;
  *) exit 0 ;;
esac

cd "$PROJECT_ROOT" || exit 0

output=$(./gradlew composeApp:ktlintCheck shared:ktlintCheck composeApp:detekt shared:detekt 2>&1)
status=$?

if [ "$status" -ne 0 ]; then
  jq -n --arg reason "ktlint/detekt failed — fix these before committing:

$output" '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: $reason
    }
  }'
fi

exit 0
