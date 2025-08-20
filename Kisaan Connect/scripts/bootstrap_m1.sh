#!/usr/bin/env bash
set -euo pipefail
if ! ./gradlew --version >/dev/null 2>&1; then
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.9
  else
    echo "Gradle not installed. Install Gradle or run 'gradle wrapper' once available."
  fi
fi
echo "Bootstrap complete."

