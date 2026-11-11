#!/bin/sh
# Called by `mvn validate` via exec-maven-plugin.
# Installs the pre-commit hook that warns on entity changes without migrations.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$SCRIPT_DIR/.."
HOOK_SRC="$ROOT/.githooks/pre-commit"
HOOK_DST="$ROOT/.git/hooks/pre-commit"

if [ ! -f "$HOOK_SRC" ]; then
  echo "install-hooks: .githooks/pre-commit not found, skipping."
  exit 0
fi

cp "$HOOK_SRC" "$HOOK_DST"
chmod +x "$HOOK_DST"
echo "install-hooks: pre-commit hook installed."
