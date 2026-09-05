#!/bin/sh
DEVICE=redroid:5555
SHOT=artifact/screenshots/discovery-with-device.png
TAGS="NsdDiscovery Resolver EventToDeviceConverter DiscoveryManager MulticastLock UnicastDiscovery NsdService serviceDiscovery"

mkdir -p artifact/screenshots artifact/diagnostics
VERSION=${DRONE_TAG:-0.00}

for apk in syncloud/build/outputs/apk/release/*.apk; do
    [ -f "$apk" ] && cp "$apk" "artifact/syncloud-$VERSION.apk"
done
for aab in syncloud/build/outputs/bundle/release/*.aab; do
    [ -f "$aab" ] && cp "$aab" "artifact/syncloud-$VERSION.aab"
done
cp syncloud/build/outputs/roborazzi/*.png artifact/screenshots/ 2>/dev/null || true

timeout 30 adb connect $DEVICE >/dev/null 2>&1 || true
timeout 60 adb -s $DEVICE exec-out run-as org.syncloud.android \
    cat files/screenshots/discovery-with-device.png > "$SHOT" 2>/dev/null || true
head -c 8 "$SHOT" 2>/dev/null | grep -q PNG || rm -f "$SHOT"

timeout 60 adb -s $DEVICE logcat -d -s $TAGS > artifact/discovery-logcat.txt 2>/dev/null || true
timeout 90 adb -s $DEVICE logcat -d > artifact/diagnostics/logcat-full.txt 2>/dev/null || true
cp instrument.log artifact/instrument.log 2>/dev/null || true

ls -la artifact artifact/screenshots
