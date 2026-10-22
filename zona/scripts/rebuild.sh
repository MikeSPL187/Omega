#!/usr/bin/env bash
# Rebuild zona/work/apktool into a signed, installable APK.
# Needs Android SDK build-tools (zipalign, apksigner) on PATH.
#
# Usage: zona/scripts/rebuild.sh [out.apk]
#   KEYSTORE=... KEY_ALIAS=... to use your own key (a debug key is made otherwise)
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
WORK="$ROOT/work"
OUT=${1:-"$WORK/zona-mod.apk"}
KEYSTORE=${KEYSTORE:-"$WORK/mod.keystore"}
KEY_ALIAS=${KEY_ALIAS:-mod}

for t in zipalign apksigner keytool; do
  command -v "$t" >/dev/null || { echo "missing $t (Android SDK build-tools / JDK)" >&2; exit 1; }
done

java -jar "$WORK/tools/apktool.jar" b "$WORK/apktool" -o "$WORK/unsigned.apk"
zipalign -f -p 4 "$WORK/unsigned.apk" "$WORK/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass android -keypass android -dname "CN=mod"
fi
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" \
  --ks-pass pass:android --key-pass pass:android --out "$OUT" "$WORK/aligned.apk"
apksigner verify "$OUT"
echo "built $OUT"
