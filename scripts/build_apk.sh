#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK21_DEFAULT="/root/.local/share/mise/installs/java/21.0.2"

if [[ "${FORCE_CURRENT_JAVA:-0}" != "1" ]]; then
  if [[ -d "$JDK21_DEFAULT" ]]; then
    export JAVA_HOME="$JDK21_DEFAULT"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

echo "[info] JAVA_HOME=${JAVA_HOME:-<unset>}"
java -version || true

echo "[info] Building debug APK..."
set +e
gradle -p "$ROOT_DIR" assembleDebug --stacktrace --info
status=$?
set -e

if [[ $status -eq 0 ]]; then
  APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
  if [[ -f "$APK_PATH" ]]; then
    echo "[ok] APK generated: $APK_PATH"
  else
    echo "[warn] Build finished but APK file not found in expected path: $APK_PATH"
  fi
  exit 0
fi

echo "[error] Could not generate APK in this environment."
echo "[hint] If the error includes HTTP 403 while resolving plugins/dependencies, the environment blocks access to Maven repositories."
exit $status
