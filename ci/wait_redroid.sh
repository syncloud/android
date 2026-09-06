#!/bin/sh -e
DEVICE=redroid:5555

getent hosts redroid || true

for i in $(seq 1 90); do
    adb disconnect $DEVICE >/dev/null 2>&1 || true
    adb connect $DEVICE >/dev/null 2>&1 || true
    if [ "$(adb -s $DEVICE shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
        echo "redroid ready after $(( i * 10 ))s"
        break
    fi
    if [ $(( i % 15 )) = 0 ]; then
        echo "still waiting for redroid after $(( i * 10 ))s"
        adb kill-server >/dev/null 2>&1 || true
    fi
    sleep 10
done

adb devices

if [ "$(adb -s $DEVICE shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; then
    echo "redroid never became ready"
    getent hosts redroid || true
    adb connect $DEVICE || true
    adb devices
    exit 1
fi

adb -s $DEVICE shell getprop ro.build.version.sdk
