#!/usr/bin/env bash
# Bouwt de echte app (APK) in een Claude-cloud-sessie. Op de laptop is dit niet nodig: daar doet Android Studio het.
#   hulpmiddelen/cloud/bouw.sh            → app/build/outputs/apk/debug/app-debug.apk
#   hulpmiddelen/cloud/bouw.sh test       → ook de unit-tests draaien
set -euo pipefail
cd "$(dirname "$0")/../.."
SDK=/opt/android-sdk

# 1. Android SDK (eenmalig per sessie, van dl.google.com)
if [ ! -d "$SDK/platforms/android-35" ]; then
    mkdir -p "$SDK/cmdline-tools"
    tmp=$(mktemp -d)
    curl -sS -o "$tmp/clt.zip" https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    echo "2d2d50857e4eb553af5a6dc3ad507a17adf43d115264b1afc116f95c92e5e258  $tmp/clt.zip" | sha256sum -c -
    unzip -q "$tmp/clt.zip" -d "$SDK/cmdline-tools" && mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
    yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null || true
    "$SDK/cmdline-tools/latest/bin/sdkmanager" "platforms;android-35" "build-tools;35.0.0" > /dev/null
fi
[ -f local.properties ] || echo "sdk.dir=$SDK" > local.properties

# 2. Maven Central geeft hier wisselend 404: downloads via de spiegel die opnieuw probeert.
if ! curl -s -o /dev/null http://127.0.0.1:8765/; then
    (cd hulpmiddelen/voorproef && setsid nohup python3 -I maven_spiegel.py 8765 > /dev/null 2>&1 < /dev/null &)
    sleep 1
fi
mkdir -p ~/.gradle/init.d
cp hulpmiddelen/cloud/spiegel.init.gradle.kts ~/.gradle/init.d/

# 3. Bouwen
taken=(assembleDebug)
[ "${1:-}" = test ] && taken+=(testDebugUnitTest)
./gradlew "${taken[@]}" --console=plain
