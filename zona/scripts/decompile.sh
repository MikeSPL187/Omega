#!/usr/bin/env bash
# Decompile an APK into zona/work/: apktool (smali + resources, rebuildable)
# and jadx (readable Java, for analysis only), then rebuild the
# obfuscated-class -> original-name maps from Kotlin metadata.
#
# Usage: zona/scripts/decompile.sh path/to/zona.apk
set -euo pipefail

APK=$(realpath "${1:?usage: decompile.sh <apk>}")
ROOT=$(cd "$(dirname "$0")/.." && pwd)
WORK="$ROOT/work"
TOOLS="$WORK/tools"
JADX_VER=1.5.1
APKTOOL_VER=2.10.0

mkdir -p "$TOOLS"
if [ ! -x "$TOOLS/jadx/bin/jadx" ]; then
  curl -sSL -o "$TOOLS/jadx.zip" "https://github.com/skylot/jadx/releases/download/v$JADX_VER/jadx-$JADX_VER.zip"
  unzip -q -o "$TOOLS/jadx.zip" -d "$TOOLS/jadx"
fi
if [ ! -f "$TOOLS/apktool.jar" ]; then
  curl -sSL -o "$TOOLS/apktool.jar" "https://github.com/iBotPeaches/Apktool/releases/download/v$APKTOOL_VER/apktool_$APKTOOL_VER.jar"
fi

java -jar "$TOOLS/apktool.jar" d -f -o "$WORK/apktool" "$APK"
# jadx exits non-zero on a handful of undecompilable methods; that is expected.
"$TOOLS/jadx/bin/jadx" -d "$WORK/jadx" --show-bad-code -j "$(nproc)" "$APK" || true

SRC="$WORK/jadx/sources"
MAP="$ROOT/mapping"
mkdir -p "$MAP"

# kotlinx.serialization descriptors keep the original FQCN: new D0("ru.zona.X", ...)
{
  printf 'obfuscated_class\tserial_name\n'
  grep -rnoE 'new [A-Za-z0-9]+\("(ru\.zona|mobi\.zona)[^"]*"' --include=*.java "$SRC" \
    | sed -E "s|^$SRC/([^:]+)\.java:[0-9]+:new [A-Za-z0-9]+\(\"([^\"]+)\"|\1\t\2|" \
    | sort -u -t$'\t' -k2
} > "$MAP/serial_names.tsv"

# Kotlin SMAP (@SourceDebugExtension) keeps the original source path of each class
{
  printf 'obfuscated_class\toriginal_source\n'
  grep -rnoE 'SMAP\\n[^\\]+\\nKotlin\\n\*S Kotlin\\n\*F\\n\+ 1 [^\\]+\\n[^\\]+' --include=*.java "$SRC" \
    | sed -E "s|^$SRC/([^:]+)\.java:[0-9]+:SMAP\\\\n[^\\\\]+\\\\n.*\\\\n\+ 1 [^\\\\]+\\\\n(.+)$|\1\t\2|" \
    | grep -E $'\t(ru|mobi)/zona' | sort -u
} > "$MAP/smap_sources.tsv"

echo "apktool: $WORK/apktool"
echo "jadx:    $WORK/jadx"
wc -l "$MAP"/*.tsv
