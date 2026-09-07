#!/bin/sh -e
DEVICE=$(ci/adb_device.sh)
RUNNER=org.syncloud.android.test/androidx.test.runner.AndroidJUnitRunner

adb devices

adb -s $DEVICE shell getprop ro.build.version.sdk
adb -s $DEVICE install -r -t syncloud/build/outputs/apk/debug/*.apk
adb -s $DEVICE install -r -t syncloud/build/outputs/apk/androidTest/debug/*.apk
adb -s $DEVICE shell am instrument -w $RUNNER 2>&1 | tee instrument.log
grep -q 'OK (' instrument.log
