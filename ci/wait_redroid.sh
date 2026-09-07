#!/bin/sh -e
for i in $(seq 1 90); do
    DEVICE=$(ci/adb_device.sh 2>/dev/null || true)
    if [ -n "$DEVICE" ] && \
       [ "$(adb -s "$DEVICE" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
        echo "redroid ready on $DEVICE after $(( i * 10 ))s"
        adb -s "$DEVICE" shell getprop ro.build.version.sdk
        exit 0
    fi
    if [ $(( i % 15 )) = 0 ]; then
        echo "still waiting for redroid after $(( i * 10 ))s"
        adb kill-server >/dev/null 2>&1 || true
        rm -f .adb-device
    fi
    sleep 10
done

echo "redroid never became reachable"
echo "dns:"; getent hosts redroid || true
echo "our interfaces:"; ip -o -4 addr show || true
echo "adb:"; adb devices || true
exit 1
