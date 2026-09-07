#!/bin/sh -e
DEVICE=redroid:5555
PKG=org.syncloud.android
LOG=artifact/diagnostics/smoke-logcat.txt

mkdir -p artifact/diagnostics
adb connect $DEVICE >/dev/null 2>&1 || true
adb -s $DEVICE uninstall $PKG >/dev/null 2>&1 || true
adb -s $DEVICE install -r syncloud/build/outputs/apk/release/*.apk
adb -s $DEVICE logcat -c

adb -s $DEVICE shell am start -W -n $PKG/.ui.DevicesSavedActivity | tee start.log
grep -q "Status: ok" start.log
sleep 15

adb -s $DEVICE logcat -d > $LOG 2>/dev/null || true
if grep -q "FATAL EXCEPTION" $LOG; then
    grep -B 2 -A 25 "FATAL EXCEPTION" $LOG
    echo "release build crashed on launch"
    exit 1
fi

PID=$(adb -s $DEVICE shell pidof $PKG | tr -d '\r')
if [ -z "$PID" ]; then
    tail -50 $LOG
    echo "release build not running after launch"
    exit 1
fi
echo "minified release launched and alive pid=$PID"
